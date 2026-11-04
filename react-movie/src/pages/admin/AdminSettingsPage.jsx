import { useState } from 'react';
import HallsTab from '../../components/admin/HallsTab.jsx';
import TicketTypesTab from '../../components/admin/TicketTypesTab.jsx';
import MembersTab from '../../components/admin/MembersTab.jsx';

const TABS = [
  { key: 'halls', text: '影廳' },
  { key: 'types', text: '票種' },
  { key: 'members', text: '會員' },
];

// 影廳 / 票種 / 會員：/admin/settings
function AdminSettingsPage() {
  const [tab, setTab] = useState('halls');

  return (
    <div className="admin-page">
      <div className="page-head">
        <div>
          <h1>影廳 / 票種 / 會員</h1>
          <p>基本資料設定</p>
        </div>
      </div>

      <div className="tabs">
        {TABS.map((t) => (
          <button key={t.key} className={tab === t.key ? 'active' : ''} onClick={() => setTab(t.key)}>
            {t.text}
          </button>
        ))}
      </div>

      {tab === 'halls' && <HallsTab />}
      {tab === 'types' && <TicketTypesTab />}
      {tab === 'members' && <MembersTab />}
    </div>
  );
}

export default AdminSettingsPage;
