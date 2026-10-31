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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.movie.Repository.MovieImageRepository;
import com.example.movie.Repository.MovieRepository;
import com.example.movie.exception.MovieNotFoundException;
import com.example.movie.model.Movie;
import com.example.movie.model.MovieImage;

import jakarta.transaction.Transactional;

@Service
public class MovieService {

	@Autowired
	MovieRepository dao;

	@Autowired
	MovieImageRepository imgdao;

	// ===================== 查詢 =====================

	// 查詢上架中的電影（分頁）
	public Page<Movie> findIsActive(int page, int size) {
		return dao.findByIsActiveTrue(buildPageable(page, size));
	}

	// 查詢下架的電影（分頁）
	public Page<Movie> findIsInactive(int page, int size) {
		return dao.findByIsActiveFalse(buildPageable(page, size));
	}

	public Movie findMovieByMovieId(int id) {
		return dao.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
	}

	private Pageable buildPageable(int page, int size) {
		if (page < 1)
			page = 1;
		if (size < 1 || size > 100)
			size = 10;
		return PageRequest.of(page - 1, size, Sort.by("movieId").ascending());
	}

	@Value("${app.upload-dir}")
	private String uploadDir;
	private static final List<String> allowExt = List.of("jpg", "jpeg", "png", "webp");
	private static final List<String> allowType = List.of("poster", "still", "banner");
	private static final List<String> allowVideoExt = List.of("mp4", "webm");

	// ===================== 新增電影 =====================
	// 流程：1.全部檢查 → 2.存 Movie 取得 movieId → 3.存圖片檔（依 imageType 改名）→ 4.存
	// MovieImage（相對路徑）
	// → 5.存預告片檔 → 6.更新 Movie 的 trailerUrl
	// 只要檢查沒過，就不會存資料表，也不會存任何檔案

	@Transactional
	public Movie createMovie
	(
		Movie movie, 
		List<MovieImage> movieImageInfo, 
		List<MultipartFile> imgfile,
		MultipartFile trailer
	) 
	{
		boolean hasTrailer = (trailer != null && !trailer.isEmpty());
		String trailerExt = null;
		if (hasTrailer) 
		{
			checkTrailer(trailer); // 檢查預告片副檔名
			trailerExt = getExt(trailer.getOriginalFilename()); // 取得預告片副檔名
		}

		// 檢查圖片（新增時至少要一張）：數量 + 每一張的類型、副檔名
		if (imgfile == null || imgfile.isEmpty()) 
		{
			throw new IllegalArgumentException("至少要有一張圖片");
		}
		checkAllImages(movieImageInfo, imgfile);

		// 檢查片名 + 上映日期是否重複
		int count = dao.countMysqlTitleAndReleaseDate(movie.getTitle(), movie.getReleaseDate());
		if (count > 0) 
		{
			throw new IllegalArgumentException("電影名稱與上映日期重複");
		}

		// ---- 到這裡全部檢查通過，才開始寫入 ----

		// 存 Movie，取得 movieId（資料夾名稱、圖片路徑要用）
		if (movie.getIsActive() == null) {
			movie.setIsActive(true); //預設上架
		}
		Movie saved = dao.save(movie);
		Integer movieId = saved.getMovieId();

		Path dir = imageDir(movieId);
		Path videoDir = videoDir(movieId);

		try 
		{
			// imageType 決定新檔名（poster1.jpg、still1.png…），存圖片檔
			List<String> fileNames = buildImageFileNames(movieImageInfo, imgfile);
			writeImageFiles(imgfile, fileNames, dir);

			// 存 MovieImage（相對路徑 /images/movies/{movieId}/poster1.jpg）
			saveImageRecords(saved, movieImageInfo, fileNames);
			
			if (hasTrailer) 
			{
				String trailerName = writeTrailerFile(trailer, trailerExt, videoDir);
				saved.setTrailerUrl(trailerUrl(movieId, trailerName));
				dao.save(saved);
			}
		} 
		catch (IOException e) 
		{
			deleteFolder(dir); // 刪掉已經寫進去的圖片
			deleteFolder(videoDir); // 刪掉已經寫進去的預告片
			throw new RuntimeException("儲存失敗: " + e.getMessage(), e); // 500，交易 rollback
		} 
		catch (RuntimeException e) 
		{
			deleteFolder(dir);
			deleteFolder(videoDir);
			throw e; // 原樣丟出，交易 rollback
		}

		return saved;
	}

	// ===================== 修改電影 =====================
	// 流程：1.全部檢查 → 2.刪除原本的圖檔 / 預告片檔 → 3.存新的圖檔 / 預告片檔
	// → 4.update Movie、MovieImage 資料表
	// 沒附圖檔 → 圖片不動；沒附預告片 → 預告片不動

	@Transactional
	public Movie updateMovie
	(int movieId, 
	 Movie form, 
	 List<MovieImage> movieImageInfo, 
	 List<MultipartFile> imagefile,
	 MultipartFile trailer) 
	{
		Movie movie = findMovieByMovieId(movieId);
		boolean hasNewImages = (imagefile != null && !imagefile.isEmpty()); // 圖片
		boolean hasNewTrailer = (trailer != null && !trailer.isEmpty()); // 預告片

		// 1-1. 檢查預告片副檔名
		String trailerExt = null;
		if (hasNewTrailer) {
			checkTrailer(trailer);
			trailerExt = getExt(trailer.getOriginalFilename());
		}

		// 1-2. 檢查圖片：數量 + 每一張的類型、副檔名
		if (hasNewImages) {
			checkAllImages(movieImageInfo, imagefile);
		}

		// 1-3. 檢查片名 + 上映日期是否被別部電影使用
		int count = dao.countMysqlTitleAndReleaseDateExcludeId
				(form.getTitle(), form.getReleaseDate(), movieId);
		if (count > 0) {
			throw new IllegalArgumentException("電影名稱與上映日期重複");
		}

		// ---- 到這裡全部檢查通過，才開始刪檔、寫入 ----

		Path imgDir = imageDir(movieId);
		Path videoDir = videoDir(movieId);
		List<String> fileNames = null;
		String trailerName = null;

		try {
			// 刪除原本實體圖檔 → 存新的實體圖檔
			if (hasNewImages) 
			{
				fileNames = buildImageFileNames(movieImageInfo, imagefile);
				FileSystemUtils.deleteRecursively(imgDir);
				writeImageFiles(imagefile, fileNames, imgDir);
			}

			// 刪除原本實體預告片檔 → 存新的實體預告片檔（只會有一個檔）
			if (hasNewTrailer) 
			{
				FileSystemUtils.deleteRecursively(videoDir);
				trailerName = writeTrailerFile(trailer, trailerExt, videoDir);
			}
		} 
		catch (IOException e) 
		{
			throw new RuntimeException("更新失敗: " + e.getMessage(), e); // 500，交易 rollback
		}

		// update 資料表 movie是從資料庫查到的資料，form是前端的資料
		// updateMovieFields在做資料轉換
		updateMovieFields(movie, form); 

		if (hasNewImages) {
			clearImageRecords(movie); // 刪掉舊的 MovieImage
			saveImageRecords(movie, movieImageInfo, fileNames); // 存 MovieImage 到資料表
		}

		if (hasNewTrailer) {
			movie.setTrailerUrl(trailerUrl(movieId, trailerName));
		}

		return dao.save(movie);
	}

	// from 是前端，movie 是Movie movie = findMovieByMovieId(movieId);  // ① 從資料庫查出來的舊資料
	private void updateMovieFields(Movie movie, Movie form) 
	{
		movie.setTitle(form.getTitle());
		movie.setDurationMin(form.getDurationMin());
		movie.setRating(form.getRating());
		movie.setSynopsis(form.getSynopsis());
		movie.setDirector(form.getDirector());
		movie.setActors(form.getActors());
		movie.setGenre(form.getGenre());
		movie.setReleaseDate(form.getReleaseDate());
		if (form.getIsActive() != null) 
		{
			movie.setIsActive(form.getIsActive()); // 沒傳就維持原本的上下架狀態
		}
	}

	// ===================== 檢查（Validation） =====================
	// 檢查不通過就丟 IllegalArgumentException（400）

	// 取得副檔名（小寫）；沒有檔名或沒有 "." 就回傳 null
	private String getExt(String fileName) {
		int dot = (fileName == null) ? -1 : fileName.lastIndexOf('.');
		if (dot == -1) {
			return null;
		}
		return fileName.substring(dot + 1).toLowerCase();
	}

	// 檢查預告片
	private void checkTrailer(MultipartFile trailer) 
	{
		String ext = getExt(trailer.getOriginalFilename());
		if (ext == null)
		{
			throw new IllegalArgumentException("預告片缺少副檔名");
		}
		if (!allowVideoExt.contains(ext)) {
			throw new IllegalArgumentException("預告片只能上傳 mp4、webm");
		}
	}


	// 檢查單張圖片（i 從 0 開始，訊息顯示第 i + 1 張）
	private void checkImage
	(MovieImage info, MultipartFile imgfiles, int i) 
	{
		String type = info.getImageType();
		String ext = getExt(imgfiles.getOriginalFilename());

		if (imgfiles.isEmpty()) {
			throw new IllegalArgumentException("第" + (i + 1) + "張圖片是空的");
		}
		if (type == null || !allowType.contains(type.toLowerCase())) {
			throw new IllegalArgumentException("圖片類型只能是 poster, still, banner");
		}
		if (ext == null) {
			throw new IllegalArgumentException("第" + (i + 1) + "張圖片缺少副檔名");
		}
		if (!allowExt.contains(ext)) {
			throw new IllegalArgumentException("只能上傳 jpg、jpeg、png、webp");
		}
	}

	// 檢查全部圖片：數量一致 + 每一張都通過
	private void checkAllImages(List<MovieImage> movieImageInfo, List<MultipartFile> imgfile) 
	{
		
		if (movieImageInfo == null || movieImageInfo.size() != imgfile.size()) 
		{
			throw new IllegalArgumentException("圖片資料與圖片檔案數量不一致");
		}
		
		for (int i = 0; i < imgfile.size(); i++) 
		{
			checkImage(movieImageInfo.get(i), imgfile.get(i), i);
		}
	}

	// ===================== 圖片（MovieImage） =====================

	// 圖片資料夾：{uploadDir}/images/movies/{movieId}
	private Path imageDir(Integer movieId) 
	{
		return Paths.get(uploadDir, "images", "movies", String.valueOf(movieId));
	}

	// 依 imageType 決定每張圖的新檔名，每種類型各自編號
	// 例如 [poster, still, still] → [poster1.jpg, still1.png, still2.jpg]
	private List<String> buildImageFileNames
	(
		List<MovieImage> movieImageInfo, 
		List<MultipartFile> imgfile
	)
	{
		List<String> fileNames = new ArrayList<>();
		Map<String, Integer> typeCount = new HashMap<>();

		for (int i = 0; i < imgfile.size(); i++) 
		{
			String imageType = movieImageInfo.get(i).getImageType().toLowerCase();
			String ext = getExt(imgfile.get(i).getOriginalFilename());

			int n = typeCount.getOrDefault(imageType, 0) + 1; // 每種類型各自計數
			typeCount.put(imageType, n);

			fileNames.add(imageType + n + "." + ext);
		}
		return fileNames;
	}

	// 把圖片檔寫入硬碟（只寫檔案，不寫資料表）
	private void writeImageFiles
	(
		List<MultipartFile> imgfile, 
		List<String> fileNames, 
		Path dir
	)
	throws IOException 
	{
		Files.createDirectories(dir);

		for (int i = 0; i < imgfile.size(); i++) 
		{
			imgfile.get(i).transferTo(dir.resolve(fileNames.get(i))); // 寫入硬碟
		}
	}

	// 存 MovieImage 到資料表（檔案已經存好之後才呼叫）
	private void saveImageRecords(Movie movie, List<MovieImage> movieImageInfo, List<String> fileNames) {
		Integer movieId = movie.getMovieId();
		int stillNo = 0; // 劇照順序：第 1 張劇照 = 1、第 2 張 = 2…（跟檔名 still1、still2 一致）

		for (int i = 0; i < fileNames.size(); i++) {
			MovieImage img = movieImageInfo.get(i);
			String fileName = fileNames.get(i); // still2.jpg
			String imageType = img.getImageType().toUpperCase(); // DB 存大寫

			int sortOrder = 0; // 只有劇照要排順序
			if (imageType.equals("STILL")) {
				stillNo++;
				sortOrder = stillNo;
			}

			movie.addImage(img); // 這行寫入外鍵
			img.setMovieId(movieId); // 讓 JSON 有值
			img.setImageType(imageType);
			img.setSortOrder(sortOrder);
			img.setImageUrl("/images/movies/" + movieId + "/" + fileName); // 存相對路徑，不是硬碟路徑
			imgdao.save(img);
		}
	}

	// 刪掉電影的舊 MovieImage 資料（只刪資料表，檔案在前面已經刪過）
	private void clearImageRecords(Movie movie) 
	{
		imgdao.deleteAll(movie.getImages());
		movie.getImages().clear();
		imgdao.flush(); // 現在就執行 DELETE
	}

	// ===================== 預告片 =====================

	// 預告片資料夾：{uploadDir}/videos/movies/{movieId}
	private Path videoDir(Integer movieId) {
		return Paths.get(uploadDir, "videos", "movies", String.valueOf(movieId));
	}

	// 預告片相對路徑：/videos/movies/{movieId}/trailer.mp4
	private String trailerUrl(Integer movieId, String trailerName) {
		return "/videos/movies/" + movieId + "/" + trailerName;
	}

	// 把預告片寫入硬碟（只寫檔案，不碰資料表），回傳檔名
	private String writeTrailerFile(MultipartFile trailer, String trailerExt, Path videoDir) throws IOException {
		Files.createDirectories(videoDir);

		String trailerName = "trailer." + trailerExt;
		trailer.transferTo(videoDir.resolve(trailerName));
		return trailerName;
	}

	// ===================== 共用 =====================

	private void deleteFolder(Path folder) {
		try {
			FileSystemUtils.deleteRecursively(folder); // Spring 內建：刪掉整個資料夾
		} catch (IOException ignored) {

		}
	}
}
