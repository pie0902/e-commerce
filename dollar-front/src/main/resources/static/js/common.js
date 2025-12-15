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

// 초기화 진입점: DOMContentLoaded 타이밍/중복 호출에도 안전하게 동작
function initHeaderOnce() {
  if (initHeaderOnce._done) return;
  initHeaderOnce._done = true;
  loadHeader().then(() => {
    bindHeaderEvents();
    checkToken();
  });
}

// DOM이 이미 로드된 경우에도 헤더를 주입하도록 보강
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initHeaderOnce);
} else {
  // 이미 DOMContentLoaded 이후라면 즉시 실행
  initHeaderOnce();
}

// 헤더 동적 로드 함수
function loadHeader() {
  const tryPaths = [
    '/components/header.html',          // 절대 경로 (기본)
    'components/header.html',           // 상대 경로 (서브패스 호환)
    './components/header.html'          // 명시적 상대 경로
  ];

  // 순차 시도 유틸
  const tryFetch = (idx) => {
    if (idx >= tryPaths.length) {
      console.error('Error loading header: all paths failed');
      return Promise.resolve();
    }
    const path = tryPaths[idx];
    return fetch(path)
      .then(response => {
        if (!response.ok) throw new Error('Failed to load header');
        return response.text();
      })
      .then(html => {
        let placeholder = document.getElementById('header-placeholder');
        // placeholder가 없으면 body 맨 앞에 삽입
        if (!placeholder) {
          placeholder = document.createElement('div');
          placeholder.id = 'header-placeholder';
          document.body.prepend(placeholder);
        }
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        const headerElement = tempDiv.querySelector('header');
        if (headerElement) {
          placeholder.replaceWith(headerElement);
        } else {
          placeholder.innerHTML = html; // fallback (비정형 콘텐츠)
        }
      })
      .catch(() => tryFetch(idx + 1));
  };

  return tryFetch(0);
}

// 이벤트 바인딩 (이벤트 위임 사용)
function bindHeaderEvents() {
  document.body.addEventListener('click', function(event) {
    // 장바구니 버튼 또는 그 내부 요소 클릭 시
    if (event.target.closest('#basket')) {
      window.location.href = '/cart.html';
      return;
    }

    // 마이페이지 버튼 또는 그 내부 요소 클릭 시
    const myPageBtn = event.target.closest('#myPage') || event.target.closest('#myPageBtn');
    if (myPageBtn) {
      // 로그인 상태 여부는 checkToken 내부 로직이나 페이지 이동 후 서버 리다이렉트에 맡김
      // 이미 checkToken이 돌아서 버튼이 교체되었을 수 있으므로,
      // myPageBtn(로그인 후)은 myPage.html로, 아이콘(myPage)은 상황에 따라 동작
      if (myPageBtn.id === 'myPageBtn') {
         window.location.href = '/myPage.html';
      } else {
         // 아이콘 클릭 시: 토큰이 있으면 마이페이지, 없으면 로그인페이지
         // 간단히 처리하기 위해 일단 페이지 이동 시도 (권한 없으면 튕기게)
         const token = getTokenFromCookie();
         if(token) window.location.href = '/myPage.html';
         else window.location.href = '/loginSignup.html';
      }
      return;
    }

    // 로그인/회원가입 버튼 클릭 시
    if (event.target.closest('#loginSignupBtn')) {
      window.location.href = '/loginSignup.html';
      return;
    }
    
    // 주문내역 버튼
    if (event.target.closest('#orderPage')) {
      window.location.href = '/orderComplete.html';
      return;
    }

    // 관리자 페이지 버튼
    if (event.target.closest('#adminPageBtn')) {
       window.location.href = '/adminDashboard.html';
       return;
    }
    
    // 로그아웃 버튼
    if (event.target.closest('#logout')) {
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
              window.location.href = '/index.html';
            }
          })
          .catch(error => {
            console.error('Error:', error);
          });
        return;
    }
  });

  // 검색 관련은 폼 서브밋이라 별도 위임이나 기존 유지
  const searchForm = document.getElementById('searchForm');
  // 동적 로드된 폼이라 여기서 못 찾을 수 있음 -> 이것도 위임으로 처리하거나 loadHeader 안에서 처리해야 함.
  // 안전하게 위임으로 처리:
  document.body.addEventListener('submit', function(event) {
    if (event.target.id === 'searchForm') {
      event.preventDefault(); 
      const searchBox = event.target.querySelector('#searchBox');
      if(searchBox) {
        const searchQuery = searchBox.value;
        if (typeof fetchSearchProducts === 'function') {
            fetchSearchProducts(searchQuery);
        } else {
            window.location.href = `/index.html?search=${encodeURIComponent(searchQuery)}`;
        }
      }
    }
  });
  
  // 검색 버튼 클릭 (type=submit이라 form submit으로 처리되지만, 명시적 클릭도 처리)
  document.body.addEventListener('click', function(event) {
      if (event.target.closest('#searchBtn')) {
          // form submit 이벤트가 발생하므로 여기서는 preventDefault를 안하면 두 번 실행될 수 있음.
          // 하지만 type="submit"이면 폼 이벤트로 넘기는게 정석.
          // 여기서는 로직 제거 또는 폼이 없는 경우 대비
      }
  });
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
    let html = `
      <button id="basket">장바구니</button>
      <button id="myPageBtn">마이페이지</button>
      <button id="orderPage">주문내역</button>
      <button id="logout">로그아웃</button>
    `;

    if (role === 'SELLER') {
      html += `<button id="adminPageBtn">관리자 페이지</button>`;
    }
    
    authButtons.innerHTML = html;
  };

  const renderLoggedOut = () => {
    authButtons.innerHTML = `
      <button id="basket">장바구니</button>
      <button id="loginSignupBtn">로그인/회원가입</button>
    `;
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
