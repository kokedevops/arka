/**
 * ARKA Auth SDK - JavaScript client para autenticación
 * Versión: 1.0.0
 */

class ArkaAuth {
    constructor(apiBaseUrl = 'http://3.134.244.104:8888') {
        this.apiBaseUrl = apiBaseUrl;
        this.storageKey = 'arka_user_session';
    }

    /**
     * Login con credenciales
     * @param {string} identifier - Usuario (admin, user, demo)
     * @param {string} password - Contraseña
     * @returns {Promise<Object>} Respuesta del login
     */
    async login(identifier, password) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    identifier: identifier,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                // Guardar sesión en localStorage
                this.saveSession({
                    username: data.username,
                    authorities: data.authorities,
                    loginTime: new Date().toISOString(),
                    token: data.token || null
                });

                return {
                    success: true,
                    user: data,
                    message: 'Login exitoso'
                };
            } else {
                return {
                    success: false,
                    message: data.message || 'Error de autenticación'
                };
            }

        } catch (error) {
            return {
                success: false,
                message: 'Error de conexión con el servidor',
                error: error.message
            };
        }
    }

    /**
     * Logout del usuario
     * @returns {Promise<Object>} Respuesta del logout
     */
    async logout() {
        try {
            await fetch(`${this.apiBaseUrl}/api/auth/logout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            this.clearSession();

            return {
                success: true,
                message: 'Logout exitoso'
            };

        } catch (error) {
            // Limpiar sesión local aunque falle el servidor
            this.clearSession();
            
            return {
                success: false,
                message: 'Error al cerrar sesión, pero se limpió la sesión local',
                error: error.message
            };
        }
    }

    /**
     * Verificar el estado de autenticación
     * @returns {Promise<Object>} Estado de autenticación
     */
    async checkAuthStatus() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/api/auth/status`);
            const data = await response.json();

            return {
                success: true,
                authenticated: data.authenticated,
                user: data.authenticated ? {
                    username: data.username,
                    authorities: data.authorities
                } : null
            };

        } catch (error) {
            return {
                success: false,
                authenticated: false,
                message: 'Error al verificar estado de autenticación',
                error: error.message
            };
        }
    }

    /**
     * Obtener información de la sesión local
     * @returns {Object|null} Información del usuario o null si no hay sesión
     */
    getSession() {
        try {
            const session = localStorage.getItem(this.storageKey);
            return session ? JSON.parse(session) : null;
        } catch (error) {
            console.error('Error al obtener sesión:', error);
            return null;
        }
    }

    /**
     * Verificar si el usuario está autenticado localmente
     * @returns {boolean} True si hay una sesión activa
     */
    isAuthenticated() {
        const session = this.getSession();
        return session !== null && session.username;
    }

    /**
     * Obtener el nombre del usuario actual
     * @returns {string|null} Nombre de usuario o null
     */
    getCurrentUser() {
        const session = this.getSession();
        return session ? session.username : null;
    }

    /**
     * Verificar si el usuario tiene un rol específico
     * @param {string} role - Rol a verificar (ADMIN, USER, etc.)
     * @returns {boolean} True si el usuario tiene el rol
     */
    hasRole(role) {
        const session = this.getSession();
        if (!session || !session.authorities) return false;

        return session.authorities.some(auth => 
            auth.authority === `ROLE_${role}` || auth.authority === role
        );
    }

    /**
     * Guardar sesión en localStorage
     * @private
     */
    saveSession(sessionData) {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(sessionData));
        } catch (error) {
            console.error('Error al guardar sesión:', error);
        }
    }

    /**
     * Limpiar sesión del localStorage
     * @private
     */
    clearSession() {
        try {
            localStorage.removeItem(this.storageKey);
        } catch (error) {
            console.error('Error al limpiar sesión:', error);
        }
    }

    /**
     * Realizar petición HTTP autenticada
     * @param {string} endpoint - Endpoint relativo
     * @param {Object} options - Opciones de fetch
     * @returns {Promise<Response>} Respuesta de la petición
     */
    async authenticatedRequest(endpoint, options = {}) {
        const session = this.getSession();
        
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        // Si hay token, agregarlo a los headers
        if (session && session.token) {
            defaultOptions.headers['Authorization'] = `Bearer ${session.token}`;
        }

        // Si no hay token pero hay usuario, usar Basic Auth
        if (session && session.username && !session.token) {
            // Nota: En producción, no deberías guardar la contraseña
            // Este es solo para el ejemplo de desarrollo
            console.warn('Usando autenticación de sesión. En producción usar tokens.');
        }

        const finalOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        return fetch(`${this.apiBaseUrl}${endpoint}`, finalOptions);
    }

    /**
     * Obtener usuarios demo disponibles
     * @returns {Promise<Object>} Lista de usuarios demo
     */
    async getDemoUsers() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/api/auth/demo-users`);
            const data = await response.json();

            return {
                success: true,
                users: data.demoUsers,
                instruction: data.instruction
            };

        } catch (error) {
            return {
                success: false,
                message: 'Error al obtener usuarios demo',
                error: error.message
            };
        }
    }
}

// Ejemplo de uso
// const auth = new ArkaAuth();
// 
// // Login
// auth.login('admin', 'admin123').then(result => {
//     if (result.success) {
//         console.log('Login exitoso:', result.user);
//     } else {
//         console.error('Error en login:', result.message);
//     }
// });
//
// // Verificar si está autenticado
// if (auth.isAuthenticated()) {
//     console.log('Usuario actual:', auth.getCurrentUser());
// }
//
// // Verificar rol
// if (auth.hasRole('ADMIN')) {
//     console.log('El usuario es administrador');
// }

// Exportar para uso como módulo
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ArkaAuth;
}

// Exportar para uso global en navegador
if (typeof window !== 'undefined') {
    window.ArkaAuth = ArkaAuth;
}
