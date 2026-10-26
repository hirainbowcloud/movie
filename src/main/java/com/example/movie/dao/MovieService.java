package com.example.movie.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.movie.Repository.MovieImageRepository;
import com.example.movie.Repository.MovieRepository;
import com.example.movie.model.Info;
import com.example.movie.model.Movie;
import com.example.movie.model.MovieImage;

import jakarta.transaction.Transactional;

@Service
public class MovieService {

	@Autowired
	MovieRepository dao;

	@Autowired
	MovieImageRepository imgdao;

	public Info findIsActive() {
		Info info = new Info();
		info.setMessage("success");
		info.setMovies(dao.findIsActive());
		return info;
	}

	public Info findMovieByMovieId(int id) {
		Optional<Movie> movieUni = dao.findById(id);
		Info info = new Info();

		if (movieUni.isPresent()) {
			info.setMessage("success");
			info.setMovie(movieUni.get());
			return info;
		} else {
			info.setMessage("fail: 沒有此電影");
		}

		return info;
	}

	@Value("${app.upload-dir}")
	private String uploadDir;
	private static final List<String> allowExt = List.of("jpg", "jpeg", "png", "webp");
	private static final List<String> allowType = List.of("poster", "still", "banner");
	private static final List<String> allowVideoExt = List.of("mp4", "webm");

	@Transactional
	public Info createMovie(Movie movie, List<MovieImage> movieImage, List<MultipartFile> file, MultipartFile trailer) {

		String trailerExt = null;
		if (trailer != null && !trailer.isEmpty()) {
			String tName = trailer.getOriginalFilename();
			int tDot = (tName == null) ? -1 : tName.lastIndexOf('.');
			if (tDot == -1) {
				return fail("fail: 預告片缺少副檔名");
			}

			trailerExt = tName.substring(tDot + 1).toLowerCase();
			if (!allowVideoExt.contains(trailerExt)) {
				return fail("fail: 預告片只能上傳 mp4、webm");
			}
		}

		if (file == null || file.isEmpty()) {
			return fail("fail: 至少要有一張圖片");
		}

		if (movieImage == null || movieImage.size() != file.size()) {
			return fail("fail: 圖片資料與檔案數量不一致");
		}

		for (int i = 0; i < file.size(); i++) {
			MultipartFile multfiles = file.get(i);
			String type = movieImage.get(i).getImageType();
			String name = multfiles.getOriginalFilename();

			if (multfiles.isEmpty()) {
				return fail("fail: 第" + (i + 1) + "張圖片是空的");
			}

			if (type == null || !allowType.contains(type.toLowerCase())) {
				return fail("fail: 圖片類型只能是 poster, still, banner");
			}

			if (name == null || name.lastIndexOf('.') == -1) {
				return fail("fail: 第" + (i + 1) + "張圖片缺少副檔名");
			}

			String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
			if (!allowExt.contains(ext)) {
				return fail("fail: 只能上傳 jpg、jpeg、png、webp");
			}
		}

		int count = dao.countMysqlTitleAndReleaseDate(movie.getTitle(), movie.getReleaseDate());
		if (count > 0) {
			return fail("fail: 電影名稱與上映日期重複");
		}

		Movie saved = dao.save(movie);
		Integer movieId = saved.getMovieId();

		Path dir = Paths.get(uploadDir, "images", "movies", String.valueOf(movieId));
		Path videoDir = Paths.get(uploadDir, "videos", "movies", String.valueOf(movieId));
		Map<String, Integer> typeCount = new HashMap<>();

		try {
			Files.createDirectories(dir);

			for (int i = 0; i < file.size(); i++) {
				MultipartFile multfiles = file.get(i);
				MovieImage img = movieImage.get(i);

				String name = multfiles.getOriginalFilename();
				String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
				String imageType = img.getImageType().toLowerCase();

				int n = typeCount.getOrDefault(imageType, 0) + 1; // 每種類型各自計數
				typeCount.put(imageType, n);

				String newFileName = imageType + n + "." + ext; // poster1.jpg
				multfiles.transferTo(dir.resolve(newFileName)); // 寫入硬碟

				saved.addImage(img); // 這行寫入外鍵
				img.setMovieId(movieId); // 讓 JSON 有值
				img.setImageType(imageType.toUpperCase()); // DB 存大寫
				img.setSortOrder(imageType.equals("still") ? n : 0); // 只要劇照
				img.setImageUrl("/images/movies/" + movieId + "/" + newFileName); // 存網址，不是硬碟路徑
				imgdao.save(img);
			}

			if (trailerExt != null) {
				Files.createDirectories(videoDir);

				String trailerName = "trailer." + trailerExt;
				trailer.transferTo(videoDir.resolve(trailerName));

				saved.setTrailerUrl("/videos/movies/" + movieId + "/" + trailerName);
			}
		} catch (IOException | RuntimeException e) {
			deleteFolder(dir); // 刪掉已經寫進去的圖片
			deleteFolder(videoDir); // 刪掉已經寫進去的預告片
			throw new RuntimeException("儲存失敗: " + e.getMessage(), e);
		}

		Info info = new Info();
		info.setMessage("success");
		info.setMovie(saved);
		return info;
	}

	private void deleteFolder(Path folder) {
		try {
			FileSystemUtils.deleteRecursively(folder); // Spring 內建：刪掉整個資料夾
		} catch (IOException ignored) {
		}
	}

	private Info fail(String msg) {
		// 標記這次交易要 rollback：迴圈中途失敗時，已存的電影和圖片都會撤銷
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		Info info = new Info();
		info.setMessage(msg);
		return info;
	}

	@Transactional
	public Info updateMovie(int movieId, Movie form, List<MovieImage> movieImage, List<MultipartFile> file,
			MultipartFile trailer) {

		Movie movie = dao.findById(movieId).orElse(null);
		if (movie == null) {
			return fail("fail: 沒有此電影");
		}

		boolean hasNewImages = (file != null && !file.isEmpty());
		boolean hasNewTrailer = (trailer != null && !trailer.isEmpty());

		String trailerExt = null;
		if (hasNewTrailer) {
			String tName = trailer.getOriginalFilename();
			int tDot = (tName == null) ? -1 : tName.lastIndexOf('.');
			if (tDot == -1) {
				return fail("fail: 預告片缺少副檔名");
			}

			trailerExt = tName.substring(tDot + 1).toLowerCase();
			if (!allowVideoExt.contains(trailerExt)) {
				return fail("fail: 預告片只能上傳 mp4、webm");
			}
		}

		if (hasNewImages) {
			if (movieImage == null || movieImage.size() != file.size()) {
				return fail("fail: 圖片資料與檔案數量不一致");
			}

			for (int i = 0; i < file.size(); i++) {
				String type = movieImage.get(i).getImageType();
				String name = file.get(i).getOriginalFilename();
				int dot = (name == null) ? -1 : name.lastIndexOf('.');

				if (file.get(i).isEmpty()) {
					return fail("fail: 第" + (i + 1) + "張圖片是空的");
				}
				if (type == null || !allowType.contains(type.toLowerCase())) {
					return fail("fail: 圖片類型只能是 poster, still, banner");
				}
				if (dot == -1) {
					return fail("fail: 第" + (i + 1) + "張圖片缺少副檔名");
				}
				if (!allowExt.contains(name.substring(dot + 1).toLowerCase())) {
					return fail("fail: 只能上傳 jpg、jpeg、png、webp");
				}
			}
		}

		int count = dao.countMysqlTitleAndReleaseDateExcludeId(form.getTitle(), form.getReleaseDate(), movieId);
		if (count > 0) {
			return fail("fail: 電影名稱與上映日期重複");
		}

		movie.setTitle(form.getTitle());
		movie.setReleaseDate(form.getReleaseDate());
		// TODO

		Path imgDir = Paths.get(uploadDir, "images", "movies", String.valueOf(movieId));
		Path videoDir = Paths.get(uploadDir, "videos", "movies", String.valueOf(movieId));

		try {
			
			if (hasNewImages) {
				imgdao.deleteAll(movie.getImages());
				movie.getImages().clear();
				imgdao.flush(); // 現在就執行 DELETE

				FileSystemUtils.deleteRecursively(imgDir);
				Files.createDirectories(imgDir);

				Map<String, Integer> typeCount = new HashMap<>();

				for (int i = 0; i < file.size(); i++) {
					MultipartFile multfiles = file.get(i);
					MovieImage img = movieImage.get(i);

					String name = multfiles.getOriginalFilename();
					String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
					String imageType = img.getImageType().toLowerCase();

					int n = typeCount.getOrDefault(imageType, 0) + 1; // 每種類型各自計數
					typeCount.put(imageType, n);

					String newFileName = imageType + n + "." + ext; // poster1.jpg
					multfiles.transferTo(imgDir.resolve(newFileName)); // 寫入硬碟

					movie.addImage(img); // 這行寫入外鍵
					img.setMovieId(movieId); // 讓 JSON 有值
					img.setImageType(imageType.toUpperCase()); // DB 存大寫
					img.setSortOrder(imageType.equals("still") ? n : 0); // 只要劇照
					img.setImageUrl("/images/movies/" + movieId + "/" + newFileName); // 存網址
					imgdao.save(img);
				}
			}

			if (hasNewTrailer) {
				FileSystemUtils.deleteRecursively(videoDir);
				Files.createDirectories(videoDir);

				String trailerName = "trailer." + trailerExt;
				trailer.transferTo(videoDir.resolve(trailerName));

				movie.setTrailerUrl("/videos/movies/" + movieId + "/" + trailerName);
			}
		} catch (IOException e) {
			throw new RuntimeException("更新失敗: " + e.getMessage(), e); // 丟出去 → rollback
		}

		Info info = new Info();
		info.setMessage("success");
		info.setMovie(movie);
		return info;
	}
}
