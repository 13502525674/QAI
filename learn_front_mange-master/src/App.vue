<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style lang="scss">
  :root {
    --admin-bg: #FDEBD3;
    --admin-bg-grad-a: #f5e6d0;
    --admin-bg-grad-b: #fff3e6;
    --admin-card: #ffffff;
    --admin-card-border: #DFDBC4;
    --admin-text: #3d3d3d;
    --admin-text-secondary: #6b6b6b;
    --admin-primary: #657166;
    --morandi-blue: #99CDDB;
    --morandi-gray: #DADBC3;
    --morandi-cream: #FDEBD3;
    --morandi-pink: #F3C3B2;
    --morandi-green: #DFDBC4;
    --morandi-deep: #657166;
    --admin-shadow-lg: 0 12px 32px rgba(80, 100, 90, 0.08);
    --admin-shadow-md: 0 8px 20px rgba(80, 100, 90, 0.06);
  }

  @property --admin-flow-angle {
    syntax: '<angle>';
    inherits: false;
    initial-value: 0deg;
  }

  @keyframes admin-flow-conic {
    from {
      --admin-flow-angle: 0deg;
    }
    to {
      --admin-flow-angle: 360deg;
    }
  }

  html,body,#app,.common-layout{
    margin: 0;
    padding: 0;
    height:100%;
  }
  body {
    color: var(--admin-text);
    font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
    background: var(--admin-bg);
  }
  .xiaoshi {
    display: none;
  }

  @keyframes layout-enter-rise {
    from {
      opacity: 0;
      transform: translateY(28px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .layout-enter {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
  }

  .layout-enter--login-1 {
    animation-delay: 0s;
  }

  .layout-enter--login-2 {
    animation-delay: 2s;
  }

  .main-view-shell .search-table > .search {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
    animation-delay: 0.1s;
  }

  .main-view-shell .search-table > .table {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
    animation-delay: 0.65s;
  }

  .main-view-shell .index-top .el-col:nth-child(1) .el-card,
  .main-view-shell .index-top .el-col:nth-child(2) .el-card,
  .main-view-shell .index-top .el-col:nth-child(3) .el-card,
  .main-view-shell .index-top .el-col:nth-child(4) .el-card {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
  }

  .main-view-shell .index-top .el-col:nth-child(1) .el-card {
    animation-delay: 0.12s;
  }

  .main-view-shell .index-top .el-col:nth-child(2) .el-card {
    animation-delay: 0.24s;
  }

  .main-view-shell .index-top .el-col:nth-child(3) .el-card {
    animation-delay: 0.36s;
  }

  .main-view-shell .index-top .el-col:nth-child(4) .el-card {
    animation-delay: 0.48s;
  }

  .main-view-shell .index-center .el-col:nth-child(1) .el-card,
  .main-view-shell .index-center .el-col:nth-child(2) .el-card {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
  }

  .main-view-shell .index-center .el-col:nth-child(1) .el-card {
    animation-delay: 0.7s;
  }

  .main-view-shell .index-center .el-col:nth-child(2) .el-card {
    animation-delay: 0.82s;
  }

  .main-view-shell .index-center-01 .el-card {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
    animation-delay: 1.02s;
  }

  .main-view-shell .index-under .el-col:nth-child(1) .el-card,
  .main-view-shell .index-under .el-col:nth-child(2) .el-card {
    animation: layout-enter-rise 0.9s cubic-bezier(0.25, 0.8, 0.25, 1) both;
  }

  .main-view-shell .index-under .el-col:nth-child(1) .el-card {
    animation-delay: 1.28s;
  }

  .main-view-shell .index-under .el-col:nth-child(2) .el-card {
    animation-delay: 1.4s;
  }

  body::-webkit-scrollbar {
      width: 0px;
      height: 0px;
  }
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
    background-color: #FDEBD3;
    border-radius: 6px;
  }
  ::-webkit-scrollbar-thumb {
    background-color: #DFDBC4;
    border-radius: 6px;
  }
  .el-pagination {
    padding-bottom:10px!important;
    padding-top:10px!important;
    display:flex;
    justify-content: flex-end
  }
    // 对话框圆角
  .el-dialog__wrapper {
    .el-dialog {
      border-radius: 14px;
      border: 1px solid var(--admin-card-border);
      box-shadow: var(--admin-shadow-lg);
      overflow: hidden;
    }
  }
  .el-dialog__title {
    font-size: 15px !important;
    font-weight: 600;
    color: var(--admin-text);
    font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  }
  // 对话框样式
  .el-dialog__header {
    border-bottom: 1px solid var(--morandi-green);
    background: linear-gradient(180deg, #fffdf8 0%, #ffffff 100%);
  }

  .el-card,
  .el-table,
  .el-select-dropdown,
  .el-dropdown-menu,
  .el-picker-panel {
    border: 1px solid var(--admin-card-border);
    box-shadow: var(--admin-shadow-md);
    border-radius: 12px;
  }

  .el-table::before {
    height: 0 !important;
  }

  .el-input__inner,
  .el-textarea__inner {
    border-radius: 10px;
    border-color: #DFDBC4;
    transition: all 0.2s ease;
  }

  .el-input__inner:focus,
  .el-textarea__inner:focus {
    border-color: #99CDDB;
    box-shadow: 0 0 0 3px rgba(153, 205, 219, 0.2);
  }

  .el-button {
    border-radius: 10px;
  }

  .el-button--primary {
    background: var(--morandi-deep);
    border-color: var(--morandi-deep);
  }

  .el-button--primary:hover {
    background: #7a8b7e;
    border-color: #7a8b7e;
  }

  .el-button--success {
    background: #99CDDB;
    border-color: #99CDDB;
    color: #3d3d3d;
  }

  .el-button--success:hover {
    background: #b0d9e5;
    border-color: #b0d9e5;
  }

  .el-button--success.is-plain {
    color: #657166;
    background: rgba(153, 205, 219, 0.15);
    border-color: #99CDDB;
  }

  .el-button--success.is-plain:hover {
    background: #99CDDB;
    color: #3d3d3d;
  }

  .el-button--warning {
    background: #F3C3B2;
    border-color: #F3C3B2;
    color: #5a3d2e;
  }

  .el-button--warning:hover {
    background: #f7d3c5;
    border-color: #f7d3c5;
  }

  .el-button--warning.is-plain {
    color: #8a5a40;
    background: rgba(243, 195, 178, 0.15);
    border-color: #F3C3B2;
  }

  .el-button--warning.is-plain:hover {
    background: #F3C3B2;
    color: #5a3d2e;
  }

  .el-button--danger {
    background: #DADBC3;
    border-color: #DADBC3;
    color: #5a5a4a;
  }

  .el-button--danger:hover {
    background: #e5e6d1;
    border-color: #e5e6d1;
  }

  .el-button--danger.is-plain {
    color: #6b6b5a;
    background: rgba(218, 219, 195, 0.15);
    border-color: #DADBC3;
  }

  .el-button--danger.is-plain:hover {
    background: #DADBC3;
    color: #5a5a4a;
  }

  .el-button--info {
    background: #DFDBC4;
    border-color: #DFDBC4;
    color: #5a5a4a;
  }

  .el-button--info:hover {
    background: #ebe8d3;
    border-color: #ebe8d3;
  }

  .el-button--info.is-plain {
    color: #6b6b5a;
    background: rgba(223, 219, 196, 0.15);
    border-color: #DFDBC4;
  }

  .el-button--info.is-plain:hover {
    background: #DFDBC4;
    color: #5a5a4a;
  }

  .el-button--default.is-plain {
    color: #4a5568;
    background: rgba(101, 113, 102, 0.06);
    border-color: #DFDBC4;
  }

  .el-button--default.is-plain:hover {
    background: #DADBC3;
    color: #3d3d3d;
    border-color: #DADBC3;
  }

  .el-table th.el-table__cell {
    background-color: #FDEBD3 !important;
    color: #4a5568 !important;
    border-bottom: 1px solid #DFDBC4 !important;
  }

  .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
    background-color: #faf8f2 !important;
  }

  .el-table td.el-table__cell {
    color: #6b6b6b !important;
  }

  .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
    background-color: #f5f0e6 !important;
  }

  .search-title {
    color: #4a5568 !important;
  }

  .search {
    background-color: var(--morandi-gray) !important;
    border: 1px solid var(--morandi-green) !important;
  }

  .search-table > .search {
    position: relative;
    isolation: isolate;
    overflow: visible;
    z-index: 1;
    width: 100%;
    box-sizing: border-box;
    border-radius: 12px !important;
    border: none !important;
    transition: transform 0.28s ease, box-shadow 0.28s ease;
    background-image: linear-gradient(
      165deg,
      #f0eed8 0%,
      #e8e6d0 28%,
      #DADBC3 52%,
      #cec9b3 100%
    ) !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.82),
      inset 0 -1px 0 rgba(100, 100, 80, 0.07),
      0 2px 6px rgba(100, 100, 80, 0.08),
      0 8px 20px rgba(100, 100, 80, 0.06);
  }

  .search-table > .search:hover {
    z-index: 5;
    transform: translateY(-3px);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.92),
      inset 0 -1px 0 rgba(100, 100, 80, 0.05),
      0 4px 12px rgba(100, 100, 80, 0.1),
      0 14px 36px rgba(100, 100, 80, 0.15);
  }

  .search-table > .search::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 14px;
    padding: 2px;
    box-sizing: border-box;
    pointer-events: none;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.22s ease;
    --admin-flow-angle: 0deg;
    background: conic-gradient(
      from var(--admin-flow-angle) at 50% 50%,
      #99CDDB 0deg,
      #DFDBC4 45deg,
      #F3C3B2 90deg,
      #DADBC3 135deg,
      #FDEBD3 180deg,
      #DFDBC4 225deg,
      #99CDDB 270deg,
      #F3C3B2 315deg,
      #99CDDB 360deg
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: exclude;
    filter:
      drop-shadow(0 0 1px rgba(153, 205, 219, 0.4))
      drop-shadow(0 0 4px rgba(223, 219, 196, 0.3))
      drop-shadow(0 0 4px rgba(243, 195, 178, 0.2));
    animation: admin-flow-conic 5s linear infinite;
    animation-play-state: paused;
  }

  .search-table > .search:hover::before {
    opacity: 1;
    animation-play-state: running;
  }

  .search-table > .search > * {
    position: relative;
    z-index: 1;
  }

  .search-table > .table {
    position: relative;
    isolation: isolate;
    overflow: visible;
    z-index: 1;
    width: 100%;
    box-sizing: border-box;
    margin-top: 10px !important;
    border-radius: 12px !important;
    background-color: #ffffff !important;
    background-image: linear-gradient(
      165deg,
      #ffffff 0%,
      #fefdfb 42%,
      #fdf9f4 100%
    ) !important;
    border: none !important;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.95),
      inset 0 -1px 0 rgba(200, 210, 230, 0.14),
      0 2px 4px rgba(80, 100, 90, 0.04),
      0 8px 22px rgba(80, 100, 90, 0.06),
      0 14px 40px rgba(80, 100, 90, 0.05);
    transition: transform 0.28s ease, box-shadow 0.28s ease;
  }

  .search-table > .table:hover {
    z-index: 4;
    transform: translateY(-3px);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.98),
      inset 0 -1px 0 rgba(200, 210, 230, 0.1),
      0 4px 10px rgba(80, 100, 90, 0.06),
      0 12px 32px rgba(80, 100, 90, 0.08),
      0 20px 48px rgba(80, 100, 90, 0.08);
  }

  .search-table > .table::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 14px;
    padding: 2px;
    box-sizing: border-box;
    pointer-events: none;
    z-index: 2;
    opacity: 0;
    transition: opacity 0.22s ease;
    --admin-flow-angle: 0deg;
    background: conic-gradient(
      from var(--admin-flow-angle) at 50% 50%,
      #99CDDB 0deg,
      #DFDBC4 45deg,
      #F3C3B2 90deg,
      #DADBC3 135deg,
      #FDEBD3 180deg,
      #DFDBC4 225deg,
      #99CDDB 270deg,
      #F3C3B2 315deg,
      #99CDDB 360deg
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: exclude;
    filter:
      drop-shadow(0 0 1px rgba(153, 205, 219, 0.4))
      drop-shadow(0 0 4px rgba(223, 219, 196, 0.3))
      drop-shadow(0 0 4px rgba(243, 195, 178, 0.2));
    animation: admin-flow-conic 5s linear infinite;
    animation-play-state: paused;
  }

  .search-table > .table:hover::before {
    opacity: 1;
    animation-play-state: running;
  }

  .search-table > .table > * {
    position: relative;
    z-index: 1;
  }

  /* ========== 黑夜模式 ========== */
  html[data-theme="dark"] body {
    background: #2a2c28 !important;
  }

  html[data-theme="dark"] .el-main {
    background: #2a2c28 !important;
  }

  html[data-theme="dark"] .header {
    background: #2a2c28 !important;
    border-bottom-color: #3d3f38 !important;
  }

  html[data-theme="dark"] .header .theme-toggle {
    background: rgba(255, 255, 255, 0.08);
    border-color: #4a4c44;
  }

  html[data-theme="dark"] .header .theme-btn {
    color: #a0a090;
  }

  html[data-theme="dark"] .header .theme-btn.active {
    background: #3d3f38;
    color: #d0d0c0;
  }

  html[data-theme="dark"] .header .search-input .el-input__inner {
    background: #3d3f38;
    border-color: #4a4c44;
    color: #c0c0b0;
  }

  html[data-theme="dark"] .header-right .icon-div:last-child {
    color: #a0a090;
  }

  html[data-theme="dark"] .main-menu {
    background: #2a2c28 !important;
    border-top-color: #3d3f38 !important;
    border-bottom-color: #3d3f38 !important;
  }

  html[data-theme="dark"] .menu-item {
    border-color: #3d3f38;
    background: #363832;
  }

  html[data-theme="dark"] .menu-item:hover {
    border-color: #4a4c44;
    background: #3d3f38;
  }

  html[data-theme="dark"] .menu-item-active {
    background: #3d3f38 !important;
    border-color: #4a4c44 !important;
  }

  html[data-theme="dark"] .menu-name {
    color: #a0a090;
  }

  html[data-theme="dark"] .active {
    color: #d0d0c0;
  }

  html[data-theme="dark"] .el-aside {
    background-color: #23251f !important;
  }

  html[data-theme="dark"] .back-icon {
    background-color: #23251f !important;
  }

  html[data-theme="dark"] .logo-text {
    color: #c0c0b0 !important;
  }

  html[data-theme="dark"] .logo-text span:first-child {
    background: #3d3f38 !important;
    color: #c0c0b0 !important;
  }

  html[data-theme="dark"] .search {
    background-color: #363832 !important;
    border-color: #3d3f38 !important;
  }

  html[data-theme="dark"] .search-table > .search {
    background-image: linear-gradient(165deg, #3d3f38 0%, #363832 50%, #2e3028 100%) !important;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15), 0 8px 20px rgba(0, 0, 0, 0.1) !important;
  }

  html[data-theme="dark"] .search-table > .search:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), 0 14px 36px rgba(0, 0, 0, 0.25) !important;
  }

  html[data-theme="dark"] .search-table > .table {
    background-image: linear-gradient(165deg, #363832 0%, #313328 50%, #2a2c28 100%) !important;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1), 0 8px 22px rgba(0, 0, 0, 0.12) !important;
  }

  html[data-theme="dark"] .el-table th.el-table__cell {
    background-color: #363832 !important;
    color: #b0b0a0 !important;
    border-bottom: 1px solid #3d3f38 !important;
  }

  html[data-theme="dark"] .el-table td.el-table__cell {
    color: #a0a090 !important;
  }

  html[data-theme="dark"] .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
    background-color: #2e3028 !important;
  }

  html[data-theme="dark"] .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
    background-color: #363832 !important;
  }

  html[data-theme="dark"] .el-card {
    background-color: #2e3028;
    border-color: #3d3f38;
    color: #c0c0b0;
  }

  html[data-theme="dark"] .el-input__inner,
  html[data-theme="dark"] .el-textarea__inner {
    background-color: #2e3028;
    border-color: #3d3f38;
    color: #c0c0b0;
  }

  html[data-theme="dark"] .el-input__inner:focus,
  html[data-theme="dark"] .el-textarea__inner:focus {
    border-color: #5a5c54;
  }

  html[data-theme="dark"] .el-dialog {
    background-color: #2e3028 !important;
    border-color: #3d3f38 !important;
  }

  html[data-theme="dark"] .el-dialog__header {
    border-bottom-color: #3d3f38;
    background: linear-gradient(180deg, #333530 0%, #2e3028 100%);
  }

  html[data-theme="dark"] .el-dialog__body {
    color: #c0c0b0 !important;
  }

  html[data-theme="dark"] .search-title {
    color: #a0a090 !important;
  }

  html[data-theme="dark"] .el-pagination .el-pager li {
    background-color: #2e3028;
    color: #a0a090;
  }

  html[data-theme="dark"] .el-select-dropdown,
  html[data-theme="dark"] .el-dropdown-menu {
    background-color: #2e3028;
    border-color: #3d3f38;
    color: #c0c0b0;
  }

  html[data-theme="dark"] ::-webkit-scrollbar {
    background-color: #2a2c28;
  }

  html[data-theme="dark"] ::-webkit-scrollbar-thumb {
    background-color: #4a4c44;
  }
</style>
