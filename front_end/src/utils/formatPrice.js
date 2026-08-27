function formatPrice(price) {
  if (price === null || price === undefined || price === '') {
    return '-';
  }

  const num = Number(price);

  if (!Number.isFinite(num)) return '-';

  // Giá >= 1: luôn ít nhất 2 số lẻ
  if (num >= 1) {
    return num.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 5
    });
  }

  // Giá từ 0.01 trở lên: tối đa 5 số lẻ
  if (num >= 0.01) {
    return num.toLocaleString('en-US', {
      maximumFractionDigits: 5
    });
  }

  // Giá nhỏ hơn 0.01:
  // tìm số 0 sau dấu phẩy rồi giữ thêm 5 chữ số có nghĩa
  const str = num.toString();
  const match = str.match(/^0\.(0*)(\d+)/);

  if (match) {
    const leadingZeros = match[1].length;
    const digits = leadingZeros + 5;

    return num.toLocaleString('en-US', {
      maximumFractionDigits: digits
    });
  }

  return num.toString();
}

export default formatPrice;