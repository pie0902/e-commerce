// common.js
// Requires env.js to be loaded first, which defines window.API
const USER_API = (window.API && window.API.USER) ? window.API.USER : 'http://localhost:8082/';
const PRODUCT_API = (window.API && window.API.PRODUCT) ? window.API.PRODUCT : 'http://localhost:8083/';

function getTokenFromCookie() {
  const cookies = document.cookie.split(';');
  for (let cookie of cookies) {
    const parts = cookie.split('=');
    const name = parts[0].trim();
    if (name === 'Authorization') {
      return parts[1];
    }
  }
  return null;
}
// 토큰에서 role 정보 추출하는 함수
function getRoleFromToken(token) {
  // 토큰 디코딩 로직 구현
  // 예시로 JWT 토큰을 디코딩하는 방법을 사용
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const decodedToken = JSON.parse(atob(base64));
  return decodedToken.role;
}

document.addEventListener('DOMContentLoaded', function() {
  loadHeader().then(() => {
    bindHeaderEvents();
    checkToken();
  });
});

// 헤더 동적 로드 함수
function loadHeader() {
  return fetch('/components/header.html')
    .then(response => {
      if (!response.ok) throw new Error('Failed to load header');
      return response.text();
    })
    .then(html => {
      const placeholder = document.getElementById('header-placeholder');
      if (placeholder) {
        // placeholder div를 실제 header 내용으로 교체 (DOM 구조 유지)
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        const headerElement = tempDiv.querySelector('header');
        if (headerElement) {
            placeholder.replaceWith(headerElement);
        } else {
            placeholder.innerHTML = html; // fallback
        }
      }
    })
    .catch(error => console.error('Error loading header:', error));
}

// 이벤트 바인딩
function bindHeaderEvents() {
  // 장바구니 버튼
  const basketButton = document.getElementById('basket');
  if (basketButton) {
    basketButton.addEventListener('click', function() {
      window.location.href = '/cart.html';
    });
  }

  // 마이페이지 버튼 (기본 동작: 로그인 안되어 있으면 로그인 페이지로)
  const myPageIcon = document.getElementById('myPage');
  if (myPageIcon) {
    myPageIcon.addEventListener('click', function () {
       // checkToken에서 로그인 상태일 때 오버라이드 됨.
       // 기본값은 로그인 페이지로 이동
       window.location.href = '/loginSignup.html';
    });
  }

  // 로그인/회원가입 기본 버튼(초기 표시용)
  const loginSignupBtn = document.getElementById('loginSignupBtn');
  if (loginSignupBtn) {
    loginSignupBtn.addEventListener('click', function () {
      window.location.href = '/loginSignup.html';
    });
  }

  // 검색 버튼
  const searchButton = document.getElementById('searchBtn');
  const searchBox = document.getElementById('searchBox');

  if (searchButton && searchBox) {
    searchButton.addEventListener('click', () => {
      const searchQuery = searchBox.value;
      if (typeof fetchSearchProducts === 'function') {
          fetchSearchProducts(searchQuery);
      } else {
          // 메인 페이지가 아니면 검색 결과 페이지로 이동
          window.location.href = `/index.html?search=${encodeURIComponent(searchQuery)}`;
      }
    });
  }
  
  // 검색 폼 제출
  const searchForm = document.getElementById('searchForm');
  if (searchForm) {
    searchForm.addEventListener('submit', (event) => {
      event.preventDefault(); 
      const searchQuery = document.getElementById('searchBox').value;
      window.location.href = `/index.html?search=${encodeURIComponent(searchQuery)}`;
    });
  }
}

// 메인 페이지용 검색 함수 (index.html에 정의됨)가 없을 경우를 대비한 placeholder
if (typeof fetchSearchProducts === 'undefined') {
    // 다른 페이지에서는 이 함수가 호출될 일이 없지만, 
    // bindHeaderEvents에서 참조 에러를 방지하기 위해 안전장치 마련
}


// Ensure Font Awesome is loaded once (icons for header)
// 로그인 상태 확인 후 버튼 렌더링 (쿠키 우선, 실패 시 API 폴백)
function checkToken() {
  const authButtons = document.getElementById('authButtons');
  if (!authButtons) return;

  const renderLoggedIn = (role) => {
    authButtons.innerHTML = `
      <button id="myPageBtn">마이페이지</button>
      <button id="orderPage">주문내역</button>
      <button id="logout">로그아웃</button>
    `;

    const myPageBtn = document.getElementById('myPageBtn');
    if (myPageBtn) {
      myPageBtn.addEventListener('click', function () {
        window.location.href = 'myPage.html';
      });
    }

    const myPageIcon = document.getElementById('myPage');
    if (myPageIcon) {
      myPageIcon.addEventListener('click', function () {
        window.location.href = 'myPage.html';
      });
    }

    const orderBtn = document.getElementById('orderPage');
    if (orderBtn) {
      orderBtn.addEventListener('click', function () {
        window.location.href = 'orderComplete.html';
      });
    }

    const logoutBtn = document.getElementById('logout');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function () {
        fetch(`${USER_API}users/logout`, {
          method: 'DELETE',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          }
        })
          .then(response => {
            if (response.ok) {
              // 프론트에서 관리하는 UI용 쿠키도 제거
              document.cookie = 'Authorization=; Max-Age=0; path=/;';
              alert('로그아웃 되었습니다.');
              window.location.reload();
            }
          })
          .catch(error => {
            console.error('Error:', error);
          });
      });
    }

    if (role === 'SELLER') {
      const adminButton = document.createElement('button');
      adminButton.textContent = '관리자 페이지';
      adminButton.addEventListener('click', function () {
        window.location.href = 'adminDashboard.html';
      });
      authButtons.appendChild(adminButton);
    }
  };

  const renderLoggedOut = () => {
    authButtons.innerHTML = `
      <button id="loginSignupBtn">로그인/회원가입</button>
    `;
    const loginBtn = document.getElementById('loginSignupBtn');
    if (loginBtn) {
      loginBtn.addEventListener('click', function () {
        window.location.href = 'loginSignup.html';
      });
    }
    const myPageIcon = document.getElementById('myPage');
    if (myPageIcon) {
      myPageIcon.addEventListener('click', function () {
        window.location.href = 'loginSignup.html';
      });
    }
  };

  // 1) 쿠키에서 토큰 확인 (비 HttpOnly 환경 지원)
  let token = getTokenFromCookie();
  if (token) {
    try {
      // Bearer 접두사 제거 및 디코딩
      token = decodeURIComponent(token);
      token = token.replace(/^Bearer\s+/i, '').replace(/^Bearer%20/i, '');
      const role = getRoleFromToken(token);
      renderLoggedIn(role);
      return;
    } catch (e) {
      // 디코딩 실패 시 폴백으로 진행
    }
  }

  // 2) 쿠키를 JS에서 못 읽는(HttpOnly) 환경일 수 있으니 서버에 검증 요청
  fetch(`${USER_API}users/profile`, {
    method: 'GET',
    credentials: 'include'
  })
    .then(res => {
      if (!res.ok) throw new Error('unauthenticated');
      return res.json();
    })
    .then(user => renderLoggedIn(user.role))
    .catch(renderLoggedOut);
}

// 장바구니 함수
function addToCart(product, quantity) {
  let cart = JSON.parse(sessionStorage.getItem('cart')) || [];
  const productIndex = cart.findIndex(item => item.productId === product.id);

  if (productIndex !== -1) {
    // 상품이 이미 있으면, 수량만 업데이트
    cart[productIndex].quantity += parseInt(quantity, 10);
    console.log("Updated quantity:", cart[productIndex]);
  } else {
    // 상품이 장바구니에 없으면, 상품 정보와 함께 추가
    cart.push({
      productId: product.id,
      name: product.name,
      price: product.price,
      quantity: parseInt(quantity, 10),
      imageUrl: product.imageUrl
    });
  }

  // 변경된 장바구니 데이터를 로컬 스토리지에 저장
  sessionStorage.setItem('cart', JSON.stringify(cart));
  alert('장바구니에 상품이 추가되었습니다!');
}
