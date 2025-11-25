const toast = (() => {
    let timer;
    const node = document.getElementById('toast');
    return (message, variant = 'info') => {
        node.textContent = message;
        node.dataset.variant = variant;
        node.classList.add('visible');
        clearTimeout(timer);
        timer = setTimeout(() => node.classList.remove('visible'), 3500);
    };
})();

const state = {
    userId: null,
    currencyTotals: {},
};

const api = {
    async registerUser(data) {
        const res = await fetch('/api/users/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data),
        });
        if (!res.ok) throw new Error('Не удалось зарегистрировать пользователя');
        return res.json();
    },
    async getBalance(userId) {
        const res = await fetch(`/api/reports/balance/${userId}`);
        if (!res.ok) throw new Error('Баланс недоступен');
        return res.json();
    },
    async getMonthlyTransactions(userId, year, month) {
        const params = new URLSearchParams({year, month});
        const res = await fetch(`/api/reports/monthly/${userId}?${params}`);
        if (!res.ok) throw new Error('Нет данных по месяцу');
        return res.json();
    },
    async getTransactions(userId) {
        const res = await fetch(`/api/transaction/user/${userId}`);
        if (!res.ok) throw new Error('Не удалось получить транзакции');
        return res.json();
    },
    async createTransaction(payload) {
        const res = await fetch('/api/transaction', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error('Ошибка при создании транзакции');
        return res.json();
    },
    async getCategories() {
        const res = await fetch('/api/categories');
        if (!res.ok) throw new Error('Не удалось получить категории');
        return res.json();
    },
    async createCategory(payload) {
        const res = await fetch('/api/categories', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error('Ошибка при создании категории');
        return res.json();
    },
    async getStructure(userId) {
        const res = await fetch(`/api/reports/structure/${userId}`);
        if (!res.ok) throw new Error('Структура портфеля недоступна');
        return res.json();
    },
    async getAssets(userId) {
        const res = await fetch(`/api/reports/assets/${userId}`);
        if (!res.ok) throw new Error('Нет данных по активам');
        return res.json();
    },
    async getDividends(userId) {
        const res = await fetch(`/api/reports/dividends/${userId}`);
        if (!res.ok) throw new Error('Нет данных по дивидендам');
        return res.json();
    },
};

const selectors = {
    balance: document.getElementById('balanceValue'),
    monthly: document.getElementById('monthlySpending'),
    assetsCount: document.getElementById('assetsCount'),
    transactions: document.getElementById('transactionsList'),
    categories: document.getElementById('categoriesList'),
    structure: document.getElementById('structureList'),
    assetsByCurrency: document.getElementById('assetsByCurrency'),
    dividends: document.getElementById('dividendsList'),
    registerResult: document.getElementById('registerResult'),
    yearInput: document.getElementById('filterYear'),
    monthInput: document.getElementById('filterMonth'),
};

function formatMoney(value) {
    if (value === null || value === undefined) return '—';
    return new Intl.NumberFormat('ru-RU', {style: 'currency', currency: 'RUB', maximumFractionDigits: 0}).format(value);
}

function formatDate(date) {
    return new Date(date).toLocaleString('ru-RU', {
        day: '2-digit',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function ensureUserId() {
    if (!state.userId) throw new Error('Сначала выбери пользователя');
    return state.userId;
}

async function handleRegister(e) {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget));
    try {
        const user = await api.registerUser(data);
        selectors.registerResult.textContent = `Новый пользователь #${user.id}`;
        selectors.registerResult.classList.remove('error');
        toast('Пользователь создан');
    } catch (err) {
        selectors.registerResult.textContent = err.message;
        selectors.registerResult.classList.add('error');
        toast(err.message, 'error');
    }
}

async function handleCategory(e) {
    e.preventDefault();
    try {
        ensureUserId();
        const payload = Object.fromEntries(new FormData(e.currentTarget));
        await api.createCategory(payload);
        toast('Категория добавлена');
        e.currentTarget.reset();
        loadCategories();
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function handleTransaction(e) {
    e.preventDefault();
    try {
        const userId = ensureUserId();
        const payload = Object.fromEntries(new FormData(e.currentTarget));
        const body = {
            amount: parseFloat(payload.amount),
            type: payload.type,
            description: payload.description,
            date: payload.date,
            category: {id: Number(payload.categoryId)},
            user: {id: Number(userId)},
        };
        await api.createTransaction(body);
        toast('Транзакция сохранена');
        e.currentTarget.reset();
        loadTransactions();
        loadBalance();
        loadMonthly();
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function loadBalance() {
    try {
        const balance = await api.getBalance(ensureUserId());
        selectors.balance.textContent = formatMoney(balance);
    } catch (err) {
        selectors.balance.textContent = '—';
        toast(err.message, 'error');
    }
}

async function loadMonthly() {
    try {
        const userId = ensureUserId();
        const year = selectors.yearInput.value || new Date().getFullYear();
        const month = selectors.monthInput.value || new Date().getMonth() + 1;
        const data = await api.getMonthlyTransactions(userId, year, month);
        const expenses = data
            .filter(tx => tx.type === 'EXPENSE')
            .reduce((sum, tx) => sum + (tx.amount || 0), 0);
        selectors.monthly.textContent = formatMoney(expenses);
    } catch (err) {
        selectors.monthly.textContent = '—';
        toast(err.message, 'error');
    }
}

async function loadTransactions() {
    const node = selectors.transactions;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const list = await api.getTransactions(ensureUserId());
        if (!list.length) {
            node.textContent = 'Транзакций пока нет';
            node.classList.add('empty');
            return;
        }
        list
            .sort((a, b) => new Date(b.date) - new Date(a.date))
            .slice(0, 8)
            .forEach(tx => {
                const div = document.createElement('div');
                div.className = 'event';
                div.innerHTML = `
                    <span class="pill ${tx.type === 'EXPENSE' ? 'expense' : 'income'}">${tx.type === 'EXPENSE' ? 'Расход' : 'Доход'}</span>
                    <div>
                        <strong>${tx.description || 'Без описания'}</strong>
                        <p>${tx.category?.name || 'Категория неизвестна'}</p>
                    </div>
                    <div style="text-align:right">
                        <strong>${tx.amount?.toFixed(2) ?? 0}</strong>
                        <p>${formatDate(tx.date)}</p>
                    </div>
                `;
                node.appendChild(div);
            });
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        toast(err.message, 'error');
    }
}

async function loadCategories() {
    const node = selectors.categories;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const categories = await api.getCategories();
        if (!categories.length) {
            node.textContent = 'Нет созданных категорий';
            node.classList.add('empty');
            return;
        }
        categories.forEach(cat => {
            const badge = document.createElement('span');
            badge.className = 'badge';
            badge.style.borderColor = cat.color || 'rgba(148,163,184,0.3)';
            badge.innerHTML = `${cat.icon || '🏷️'} ${cat.name}`;
            node.appendChild(badge);
        });
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        toast(err.message, 'error');
    }
}

async function loadStructure() {
    const node = selectors.structure;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const data = await api.getStructure(ensureUserId());
        const entries = Object.entries(data);
        if (!entries.length) {
            node.textContent = 'Портфель пуст';
            node.classList.add('empty');
            return;
        }
        entries.forEach(([type, qty]) => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${type}</span><strong>${qty?.toFixed(2)}</strong>`;
            node.appendChild(li);
        });
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        toast(err.message, 'error');
    }
}

async function loadAssets() {
    const node = selectors.assetsByCurrency;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const map = await api.getAssets(ensureUserId());
        const entries = Object.entries(map);
        selectors.assetsCount.textContent = entries.reduce((sum, [, assets]) => sum + assets.length, 0);
        if (!entries.length) {
            node.textContent = 'Активы не найдены';
            node.classList.add('empty');
            return;
        }
        entries.forEach(([currency, assets]) => {
            const card = document.createElement('div');
            card.className = 'currency-card';
            card.innerHTML = `<h4>${currency}</h4>`;
            const ul = document.createElement('ul');
            assets.forEach(asset => {
                const li = document.createElement('li');
                li.textContent = `${asset.name} • ${asset.quantity ?? 0}`;
                ul.appendChild(li);
            });
            card.appendChild(ul);
            node.appendChild(card);
        });
    } catch (err) {
        selectors.assetsCount.textContent = '—';
        node.textContent = err.message;
        node.classList.add('empty');
        toast(err.message, 'error');
    }
}

async function loadDividends() {
    const node = selectors.dividends;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const list = await api.getDividends(ensureUserId());
        if (!list.length) {
            node.textContent = 'Начислений пока нет';
            node.classList.add('empty');
            return;
        }
        list.slice(0, 6).forEach(acc => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${acc.asset?.ticker || '—'}</span><strong>${acc.amount ?? 0}</strong>`;
            node.appendChild(li);
        });
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        toast(err.message, 'error');
    }
}

async function hydrateDashboard() {
    try {
        await Promise.all([
            loadBalance(),
            loadMonthly(),
            loadTransactions(),
            loadCategories(),
            loadStructure(),
            loadAssets(),
            loadDividends(),
        ]);
        toast('Данные обновлены');
    } catch (err) {
        toast(err.message, 'error');
    }
}

function init() {
    document.getElementById('loadUserBtn').addEventListener('click', () => {
        const input = document.getElementById('userIdInput');
        const id = Number(input.value);
        if (!id) {
            toast('Введите корректный ID');
            return;
        }
        state.userId = id;
        hydrateDashboard();
    });

    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('categoryForm').addEventListener('submit', handleCategory);
    document.getElementById('transactionForm').addEventListener('submit', handleTransaction);
    document.getElementById('refreshMonthly').addEventListener('click', e => {
        e.preventDefault();
        loadMonthly();
    });
}

document.addEventListener('DOMContentLoaded', init);

