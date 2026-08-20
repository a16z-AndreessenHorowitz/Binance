import "./headerTradeCoin.css";
import binanceLogo from "../../assets/images/binance.png";

function handleTheme(e){
  e.preventDefault()
  document.body.classList.toggle("dark-theme")
}



function HeaderTradeCoin() {

  return <>
  <header className="headerTradeCoin d-flex">
  <div className="container-fluid">

    {/* Logo */}
    <a href="/" className="logo">
      <img src={binanceLogo}  />
    </a>

    {/* Menu trái */}
    <nav className="mainNav">
      <ul>
        <li><a href="#">Mua Crypto</a></li>
        <li><a href="#">Thị trường </a></li>
        <li><a href="#">Giao dịch <i className="fa-solid fa-chevron-down dropdownIcon"></i></a></li>
        <li><a href="#">Futures <i className="fa-solid fa-chevron-down dropdownIcon"></i></a></li>
        <li><a href="#">Earn <i className="fa-solid fa-chevron-down dropdownIcon"></i></a></li>
        <li><a href="#">Square <i className="fa-solid fa-chevron-down dropdownIcon"></i></a></li>
        <li><a href="#">Nhiều hơn <i className="fa-solid fa-chevron-down dropdownIcon"></i></a></li>
      </ul>
    </nav>

    {/* Menu phải */}
    <nav className="rightNav">
      <ul>
        <li><a href="#"><i className="fa-solid fa-magnifying-glass"></i></a></li>
        <li><a href="#" className="loginBtn">Đăng nhập</a></li>
        <li><a href="#" className="registerBtn">Đăng ký</a></li>
        <li><a href="#"><i className="fa-solid fa-download"></i></a></li>
        <li className="menu_bars"><a href="#"><i className="fa-solid fa-bars"></i></a></li>
        <li><a href="#"><i className="fa-solid fa-earth-americas"></i></a></li>
        <li><a href="#"><i className="fa-solid fa-circle-question"></i></a></li>
        <li><a href="#"><i className="fa-solid fa-gear"></i></a></li>
        <li><a href="#" onClick={handleTheme}><i className="fa-solid fa-moon"></i></a></li>
      </ul>
    </nav>

  </div>
</header>
</>
}

export default HeaderTradeCoin;
