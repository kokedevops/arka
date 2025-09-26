/**
 * Ejemplos de uso del ARKA Auth SDK en diferentes frameworks
 */

// ==========================================
// 1. VANILLA JAVASCRIPT
// ==========================================

// Inicializar el SDK
const auth = new ArkaAuth('http://3.134.244.104:8888');

// Función de login simple
async function handleLogin() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    const result = await auth.login(username, password);
    
    if (result.success) {
        alert('Login exitoso!');
        window.location.href = '/dashboard.html';
    } else {
        alert('Error: ' + result.message);
    }
}

// Verificar autenticación al cargar la página
document.addEventListener('DOMContentLoaded', async () => {
    if (auth.isAuthenticated()) {
        console.log('Usuario autenticado:', auth.getCurrentUser());
        
        // Verificar también en el servidor
        const status = await auth.checkAuthStatus();
        if (!status.authenticated) {
            // Sesión expirada, limpiar
            auth.logout();
        }
    }
});

// ==========================================
// 2. CON JQUERY
// ==========================================

$(document).ready(async function() {
    const auth = new ArkaAuth('http://3.134.244.104:8888');
    
    // Login form submit
    $('#loginForm').on('submit', async function(e) {
        e.preventDefault();
        
        const identifier = $('#identifier').val();
        const password = $('#password').val();
        
        // Mostrar loading
        $('#loginButton').prop('disabled', true).text('Iniciando sesión...');
        
        try {
            const result = await auth.login(identifier, password);
            
            if (result.success) {
                $('#message').removeClass('error').addClass('success')
                    .text('¡Login exitoso! Redirigiendo...');
                
                setTimeout(() => {
                    window.location.href = '/dashboard.html';
                }, 1500);
            } else {
                $('#message').removeClass('success').addClass('error')
                    .text(result.message);
            }
        } catch (error) {
            $('#message').removeClass('success').addClass('error')
                .text('Error de conexión');
        } finally {
            $('#loginButton').prop('disabled', false).text('Iniciar Sesión');
        }
    });
    
    // Quick login buttons
    $('.quick-login-btn').on('click', async function() {
        const username = $(this).data('username');
        const password = $(this).data('password');
        
        $('#identifier').val(username);
        $('#password').val(password);
        $('#loginForm').submit();
    });
});

// ==========================================
// 3. ESTILO REACT (usando hooks pattern)
// ==========================================

// Hook personalizado para autenticación
function useArkaAuth() {
    const [user, setUser] = React.useState(null);
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState(null);
    
    const auth = React.useMemo(() => new ArkaAuth('http://3.134.244.104:8888'), []);
    
    const login = React.useCallback(async (identifier, password) => {
        setLoading(true);
        setError(null);
        
        try {
            const result = await auth.login(identifier, password);
            
            if (result.success) {
                setUser(result.user);
                return { success: true };
            } else {
                setError(result.message);
                return { success: false, message: result.message };
            }
        } catch (error) {
            setError('Error de conexión');
            return { success: false, message: 'Error de conexión' };
        } finally {
            setLoading(false);
        }
    }, [auth]);
    
    const logout = React.useCallback(async () => {
        setLoading(true);
        try {
            await auth.logout();
            setUser(null);
        } catch (error) {
            console.error('Error en logout:', error);
        } finally {
            setLoading(false);
        }
    }, [auth]);
    
    React.useEffect(() => {
        // Verificar sesión existente al montar
        if (auth.isAuthenticated()) {
            const sessionUser = auth.getSession();
            setUser(sessionUser);
        }
    }, [auth]);
    
    return {
        user,
        loading,
        error,
        login,
        logout,
        isAuthenticated: auth.isAuthenticated(),
        hasRole: (role) => auth.hasRole(role)
    };
}

// Componente de Login en React
function LoginComponent() {
    const { login, loading, error, isAuthenticated } = useArkaAuth();
    const [identifier, setIdentifier] = React.useState('');
    const [password, setPassword] = React.useState('');
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        const result = await login(identifier, password);
        
        if (result.success) {
            // Redirigir o actualizar estado
            window.location.href = '/dashboard';
        }
    };
    
    if (isAuthenticated) {
        return <div>Ya estás autenticado!</div>;
    }
    
    return (
        <form onSubmit={handleSubmit}>
            <input
                type="text"
                placeholder="Usuario"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
            />
            <input
                type="password"
                placeholder="Contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />
            <button type="submit" disabled={loading}>
                {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </button>
            {error && <div className="error">{error}</div>}
        </form>
    );
}

// ==========================================
// 4. ESTILO VUE.JS
// ==========================================

const LoginVueComponent = {
    template: `
        <div class="login-container">
            <form @submit.prevent="handleLogin">
                <input 
                    v-model="identifier" 
                    type="text" 
                    placeholder="Usuario" 
                    required 
                />
                <input 
                    v-model="password" 
                    type="password" 
                    placeholder="Contraseña" 
                    required 
                />
                <button type="submit" :disabled="loading">
                    {{ loading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
                </button>
            </form>
            
            <div v-if="error" class="error">{{ error }}</div>
            <div v-if="user" class="success">¡Bienvenido {{ user.username }}!</div>
            
            <div class="quick-login">
                <button @click="quickLogin('admin', 'admin123')">Admin</button>
                <button @click="quickLogin('user', 'user123')">User</button>
                <button @click="quickLogin('demo', 'demo123')">Demo</button>
            </div>
        </div>
    `,
    
    data() {
        return {
            identifier: '',
            password: '',
            loading: false,
            error: null,
            user: null,
            auth: new ArkaAuth('http://3.134.244.104:8888')
        };
    },
    
    mounted() {
        // Verificar sesión existente
        if (this.auth.isAuthenticated()) {
            this.user = this.auth.getSession();
        }
    },
    
    methods: {
        async handleLogin() {
            this.loading = true;
            this.error = null;
            
            try {
                const result = await this.auth.login(this.identifier, this.password);
                
                if (result.success) {
                    this.user = result.user;
                    // Redirigir después de 2 segundos
                    setTimeout(() => {
                        this.$router.push('/dashboard');
                    }, 2000);
                } else {
                    this.error = result.message;
                }
            } catch (error) {
                this.error = 'Error de conexión';
            } finally {
                this.loading = false;
            }
        },
        
        quickLogin(username, password) {
            this.identifier = username;
            this.password = password;
            this.handleLogin();
        }
    }
};

// ==========================================
// 5. PROTECCIÓN DE RUTAS
// ==========================================

// Middleware para proteger rutas
function requireAuth(next) {
    const auth = new ArkaAuth();
    
    if (!auth.isAuthenticated()) {
        // Redirigir a login
        window.location.href = '/login.html';
        return false;
    }
    
    // Verificar también en el servidor
    auth.checkAuthStatus().then(status => {
        if (!status.authenticated) {
            auth.logout();
            window.location.href = '/login.html';
        }
    });
    
    if (next) next();
    return true;
}

// Middleware para verificar roles
function requireRole(role) {
    return function(next) {
        const auth = new ArkaAuth();
        
        if (!auth.isAuthenticated() || !auth.hasRole(role)) {
            alert('No tienes permisos para acceder a esta página');
            window.location.href = '/';
            return false;
        }
        
        if (next) next();
        return true;
    };
}

// Uso en páginas protegidas
document.addEventListener('DOMContentLoaded', () => {
    // Para páginas que requieren autenticación
    requireAuth();
    
    // Para páginas que requieren rol específico
    // requireRole('ADMIN')();
});

// ==========================================
// 6. CONFIGURACIÓN GLOBAL
// ==========================================

// Configurar interceptor global para peticiones autenticadas
class ArkaApiClient {
    constructor() {
        this.auth = new ArkaAuth();
    }
    
    async get(endpoint) {
        return this.auth.authenticatedRequest(endpoint, {
            method: 'GET'
        });
    }
    
    async post(endpoint, data) {
        return this.auth.authenticatedRequest(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    async put(endpoint, data) {
        return this.auth.authenticatedRequest(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    async delete(endpoint) {
        return this.auth.authenticatedRequest(endpoint, {
            method: 'DELETE'
        });
    }
}

// Cliente API global
window.api = new ArkaApiClient();

// Ejemplo de uso:
// api.get('/api/orders').then(response => response.json()).then(orders => console.log(orders));
// api.post('/api/products', { name: 'Nuevo Producto', price: 100 });
