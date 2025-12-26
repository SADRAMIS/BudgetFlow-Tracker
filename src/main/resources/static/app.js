const toast = (() => {
    let timer;
    const el = document.getElementById('toast');
    return (message, variant = 'info') => {
        el.textContent = message;
        el.dataset.variant = variant;
        el.classList.add('visible');
        clearTimeout(timer);
        timer = setTimeout(() => el.classList.remove('visible'), 3000);
    };
})();

const state = {
    userId: null,
    structure: {},
    dividends: [],
    targets: {
        STOCKS: 0.6,
        BONDS: 0.3,
        CASH: 0.1,
    },
};

const api = {
    async registerUser(payload) {
        const res = await fetch('/api/users/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error('Не удалось создать пользователя');
        return res.json();
    },
    async getBalance(userId) {
        return fetch(`/api/reports/balance/${userId}`).then(checkJson('Баланс недоступен'));
    },
    async getMonthly(userId, year, month) {
        const params = new URLSearchParams({year, month});
        return fetch(`/api/reports/monthly/${userId}?${params}`).then(checkJson('Нет данных за месяц'));
    },
    async getTransactions(userId) {
        return fetch(`/api/transaction/user/${userId}`).then(checkJson('Нет транзакций'));
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
        return fetch('/api/categories').then(checkJson('Не удалось получить категории'));
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
        return fetch(`/api/reports/structure/${userId}`).then(checkJson('Структура недоступна'));
    },
    async getAssets(userId) {
        return fetch(`/api/reports/assets/${userId}`).then(checkJson('Активы недоступны'));
    },
    async getDividends(userId) {
        return fetch(`/api/reports/dividends/${userId}`).then(checkJson('Нет дивидендов'));
    },
    async syncTinkoff(payload, userId) {
        const res = await fetch(`/api/integrations/tinkoff/sync?userId=${userId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error('API Т‑Инвестиций недоступно');
        return res.json();
    },
    async getPortfolioAnalytics(userId) {
        return fetch(`/api/analytics/portfolio/${userId}`).then(checkJson('Аналитика недоступна'));
    },
    async getDividendCalendar(userId, monthsAhead = 12) {
        return fetch(`/api/analytics/dividends/${userId}?monthsAhead=${monthsAhead}`)
            .then(checkJson('Календарь недоступен'));
    },
    async calculateRebalance(userId, targets) {
        const res = await fetch(`/api/analytics/rebalance/${userId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(targets),
        });
        if (!res.ok) throw new Error('Ошибка расчёта ребаланса');
        return res.json();
    },
    async createSnapshot(userId) {
        const res = await fetch(`/api/analytics/snapshot/${userId}`, {method: 'POST'});
        if (!res.ok) throw new Error('Ошибка создания снимка');
        return res.text();
    },
    async importFile(file, userId, accountNumber) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('userId', userId);
        formData.append('accountNumber', accountNumber);
        const res = await fetch('/api/import/operations', {
            method: 'POST',
            body: formData,
        });
        if (!res.ok) throw new Error('Ошибка импорта файла');
        return res.json();
    },
};

function checkJson(errorMessage) {
    return async res => {
        if (!res.ok) throw new Error(errorMessage);
        return res.json();
    };
}

const sel = id => document.getElementById(id);
const selectors = {
    balance: sel('balanceValue'),
    monthly: sel('monthlySpending'),
    assetsCount: sel('assetsCount'),
    expectedDividends: sel('expectedDividends'),
    transactions: sel('transactionsList'),
    categories: sel('categoriesList'),
    structure: sel('structureList'),
    assetsByCurrency: sel('assetsByCurrency'),
    dividends: sel('dividendsList'),
    rebalance: sel('rebalanceList'),
    registerResult: sel('registerResult'),
    tinkoffResult: sel('tinkoffResult'),
    totalValue: sel('totalValue'),
    totalCost: sel('totalCost'),
    totalReturn: sel('totalReturn'),
    sharpeRatio: sel('sharpeRatio'),
    portfolioStructure: sel('portfolioStructure'),
    dividendCalendar: sel('dividendCalendar'),
    fileImportResult: sel('fileImportResult'),
};

function ensureUserId() {
    if (!state.userId) throw new Error('Сначала загрузите пользователя');
    return state.userId;
}

function formatMoney(value, currency = 'RUB') {
    if (value === null || value === undefined) return '—';
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency,
        maximumFractionDigits: 0,
    }).format(value);
}

function formatDate(date) {
    if (!date) return '—';
    return new Date(date).toLocaleString('ru-RU', {
        day: '2-digit',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
    });
}

async function handleRegister(e) {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget));
    try {
        const user = await api.registerUser(data);
        selectors.registerResult.textContent = `Создан пользователь #${user.id}`;
        toast('Пользователь зарегистрирован');
    } catch (err) {
        selectors.registerResult.textContent = err.message;
        toast(err.message, 'error');
    }
}

async function handleCategory(e) {
    e.preventDefault();
    try {
        await api.createCategory(Object.fromEntries(new FormData(e.currentTarget)));
        toast('Категория сохранена');
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
        const data = Object.fromEntries(new FormData(e.currentTarget));
        const payload = {
            amount: Number(data.amount),
            type: data.type,
            description: data.description,
            date: data.date,
            category: {id: Number(data.categoryId)},
            user: {id: Number(userId)},
        };
        await api.createTransaction(payload);
        toast('Транзакция добавлена');
        e.currentTarget.reset();
        await Promise.all([loadTransactions(), loadBalance(), loadMonthly()]);
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function handleUserSelect(e) {
    e.preventDefault();
    const input = sel('userIdInput');
    const value = Number(input.value);
    if (!value) {
        toast('Введите корректный ID');
        return;
    }
    state.userId = value;
    hydrate();
}

function handleDemo() {
    sel('userIdInput').value = 1;
    state.userId = 1;
    hydrate();
}

async function loadBalance() {
    try {
        const value = await api.getBalance(ensureUserId());
        selectors.balance.textContent = formatMoney(value);
    } catch (err) {
        selectors.balance.textContent = '—';
        toast(err.message, 'error');
    }
}

async function loadMonthly() {
    try {
        const now = new Date();
        const data = await api.getMonthly(ensureUserId(), now.getFullYear(), now.getMonth() + 1);
        const expenses = data.filter(tx => tx.type === 'EXPENSE')
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
            node.textContent = 'Нет транзакций';
            node.classList.add('empty');
            return;
        }
        list.sort((a, b) => new Date(b.date) - new Date(a.date))
            .slice(0, 10)
            .forEach(tx => {
                const div = document.createElement('div');
                div.className = 'event';
                div.innerHTML = `
                    <span class="pill ${tx.type === 'EXPENSE' ? 'expense' : 'income'}">${tx.type}</span>
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
            node.textContent = 'Нет категорий';
            node.classList.add('empty');
            return;
        }
        categories.forEach(cat => {
            const span = document.createElement('span');
            span.className = 'badge';
            span.style.borderColor = cat.color || 'rgba(148,163,184,0.4)';
            span.textContent = `${cat.icon || '🏷️'} ${cat.name}`;
            node.appendChild(span);
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
        state.structure = data;
        const entries = Object.entries(data);
        if (!entries.length) {
            node.textContent = 'Портфель пуст';
            node.classList.add('empty');
            selectors.rebalance.classList.add('empty');
            selectors.rebalance.textContent = 'Недостаточно данных';
            return;
        }
        entries.forEach(([type, qty]) => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${type}</span><strong>${qty?.toFixed(2)}</strong>`;
            node.appendChild(li);
        });
        renderRebalance();
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        selectors.rebalance.textContent = 'Недостаточно данных';
        selectors.rebalance.classList.add('empty');
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
        selectors.assetsCount.textContent = entries.reduce((sum, [, list]) => sum + list.length, 0);
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
        node.textContent = err.message;
        node.classList.add('empty');
        selectors.assetsCount.textContent = '—';
        toast(err.message, 'error');
    }
}

async function loadDividends() {
    const node = selectors.dividends;
    node.innerHTML = '';
    node.classList.remove('empty');
    try {
        const list = await api.getDividends(ensureUserId());
        state.dividends = list;
        if (!list.length) {
            node.textContent = 'Начислений пока нет';
            node.classList.add('empty');
            selectors.expectedDividends.textContent = '—';
            return;
        }
        const total = list.reduce((sum, accrual) => sum + (accrual.amount || 0), 0);
        selectors.expectedDividends.textContent = formatMoney(total);
        list.slice(0, 6).forEach(acc => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${acc.asset?.ticker || acc.asset?.name || '—'}</span>
                <strong>${acc.amount ?? 0}</strong>
            `;
            node.appendChild(li);
        });
    } catch (err) {
        node.textContent = err.message;
        node.classList.add('empty');
        selectors.expectedDividends.textContent = '—';
        toast(err.message, 'error');
    }
}

function renderRebalance() {
    const node = selectors.rebalance;
    node.innerHTML = '';
    node.classList.remove('empty');
    const entries = Object.entries(state.targets);
    const total = Object.values(state.structure).reduce((sum, val) => sum + (val || 0), 0);
    if (!total) {
        node.textContent = 'Недостаточно данных';
        node.classList.add('empty');
        return;
    }
    entries.forEach(([type, targetShare]) => {
        const current = state.structure[type] || 0;
        const currentShare = total ? current / total : 0;
        const delta = ((targetShare - currentShare) * 100).toFixed(1);
        const div = document.createElement('div');
        div.className = 'rebalance-item';
        div.innerHTML = `
            <div>
                <strong>${type}</strong>
                <p class="hint">Текущая доля: ${(currentShare * 100).toFixed(1)}%</p>
            </div>
            <span class="delta ${delta >= 0 ? 'positive' : 'negative'}">${delta}%</span>
        `;
        node.appendChild(div);
    });
}

async function handleTinkoff(e) {
    e.preventDefault();
    try {
        const userId = ensureUserId();
        const payload = Object.fromEntries(new FormData(e.currentTarget));
        const response = await api.syncTinkoff(payload, userId);
        const ops = response?.summary?.estimatedOps;
        const summary = ops ? `~${ops} операций` : '';
        selectors.tinkoffResult.textContent = `${response.message}. Режим: ${response.mode}. ${summary}`;
        toast(response.message);
        setTimeout(() => hydrate(), 2000);
    } catch (err) {
        selectors.tinkoffResult.textContent = err.message;
        toast(err.message, 'error');
    }
}

async function handleFileImport(e) {
    e.preventDefault();
    try {
        const userId = ensureUserId();
        const formData = new FormData(e.currentTarget);
        const file = formData.get('file');
        const accountNumber = formData.get('accountNumber');
        
        if (!file || !file.size) {
            throw new Error('Выберите файл');
        }
        
        const result = await api.importFile(file, userId, accountNumber);
        const message = result.success 
            ? `Импортировано: ${result.operationsCount} операций, ${result.assetsCount} активов`
            : `Ошибки: ${result.errors.join(', ')}`;
        selectors.fileImportResult.textContent = message;
        toast(result.success ? 'Файл импортирован' : 'Ошибки при импорте', result.success ? 'info' : 'error');
        if (result.success) {
            setTimeout(() => hydrate(), 1000);
        }
    } catch (err) {
        selectors.fileImportResult.textContent = err.message;
        toast(err.message, 'error');
    }
}

async function loadAnalytics() {
    try {
        const userId = ensureUserId();
        const analytics = await api.getPortfolioAnalytics(userId);
        
        selectors.totalValue.textContent = formatMoney(analytics.totalValue);
        selectors.totalCost.textContent = formatMoney(analytics.totalCost);
        selectors.totalReturn.textContent = `${analytics.totalReturn.toFixed(2)}%`;
        selectors.totalReturn.style.color = analytics.totalReturn >= 0 ? '#10b981' : '#ef4444';
        selectors.sharpeRatio.textContent = analytics.sharpeRatio.toFixed(2);
        
        // Структура портфеля
        const structureNode = selectors.portfolioStructure;
        structureNode.innerHTML = '';
        const byType = analytics.byType || {};
        const byCurrency = analytics.byCurrency || {};
        
        const typeDiv = document.createElement('div');
        typeDiv.innerHTML = '<h4>По типам</h4><ul class="stat-list"></ul>';
        const typeList = typeDiv.querySelector('ul');
        Object.entries(byType).forEach(([type, value]) => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${type}</span><strong>${formatMoney(value)}</strong>`;
            typeList.appendChild(li);
        });
        
        const currencyDiv = document.createElement('div');
        currencyDiv.innerHTML = '<h4>По валютам</h4><ul class="stat-list"></ul>';
        const currencyList = currencyDiv.querySelector('ul');
        Object.entries(byCurrency).forEach(([currency, value]) => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${currency}</span><strong>${formatMoney(value)}</strong>`;
            currencyList.appendChild(li);
        });
        
        structureNode.appendChild(typeDiv);
        structureNode.appendChild(currencyDiv);
        
        // Календарь дивидендов
        const calendar = await api.getDividendCalendar(userId, 12);
        const calendarNode = selectors.dividendCalendar;
        calendarNode.innerHTML = '<h4>Ближайшие выплаты</h4><ul class="stat-list"></ul>';
        const calendarList = calendarNode.querySelector('ul');
        calendar.events.slice(0, 10).forEach(event => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${event.date} • ${event.ticker}</span>
                <strong>${formatMoney(event.amount)}</strong>
            `;
            calendarList.appendChild(li);
        });
        
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function hydrate() {
    toast('Загружаем данные…');
    await Promise.all([
        loadBalance(),
        loadMonthly(),
        loadTransactions(),
        loadCategories(),
        loadStructure(),
        loadAssets(),
        loadDividends(),
        loadAnalytics(),
    ]);
    toast('Данные обновлены');
}

function init() {
    document.getElementById('userPicker').addEventListener('submit', handleUserSelect);
    document.getElementById('demoBtn').addEventListener('click', handleDemo);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('categoryForm').addEventListener('submit', handleCategory);
    document.getElementById('transactionForm').addEventListener('submit', handleTransaction);
    document.getElementById('refreshPortfolio').addEventListener('click', e => {
        e.preventDefault();
        loadStructure();
        loadAssets();
    });
    document.getElementById('tinkoffForm').addEventListener('submit', handleTinkoff);
    document.getElementById('fileImportForm').addEventListener('submit', handleFileImport);
    document.getElementById('refreshAnalytics').addEventListener('click', e => {
        e.preventDefault();
        loadAnalytics();
    });
}

document.addEventListener('DOMContentLoaded', init);

