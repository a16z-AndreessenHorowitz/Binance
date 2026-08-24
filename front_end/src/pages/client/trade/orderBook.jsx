import "./orderBook.css"
function OrderBook({symbol}){
  



  return <>
    <div className="orderBook">
      <div className="nameBook">
        Sổ lệnh 
        <i className="fa-solid fa-ellipsis"></i>
      </div>
      <div className="orderbook-header">
        <div className="orderbook-header-tips">
          <i className="fa-solid fa-table-columns"></i>
          <i className="fa-solid fa-table-columns"></i>
          <i className="fa-solid fa-table-columns"></i>
        </div> 
        <div className="orderbook-tickSize">
          0.01 <i className="fa-solid fa-caret-down"></i>
        </div> 
      </div>  
      <div className="content">
          <div className="item">Giá(USDT)</div>
          <div className="item">Số lượng (BTC)</div>
          <div className="item">Tổng</div>
      </div>
      {/* order book */}
      <div className="orderbook-list-container">
        <div className="ask-light emit-price">
          77.222,00
        </div>
        <div className="text emit-price">
          0,00007
        </div>
        <div className="text emit-price">
          5,422287
        </div>
      </div>

      {/* price  */}
      <div className="orderbook-ticker">
        <div className="price">
          77.000,00
        </div>
        <div className="subPrice">
          77.000,00
        </div>
        <i className="fa-solid fa-chevron-right"></i>
      </div>
      {/* order book */}
      <div className="orderbook-list-container">
      </div>

    </div>
  </>
}
export default OrderBook;