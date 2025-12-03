# Authentication Sequence Diagrams - Identity Service

This document contains sequence diagrams for the authentication flows (login and registration) in the SketchNote Identity Service.

## 1. Standard Login Flow (Email/Password)

```mermaid
sequenceDiagram
    actor User
    participant Client as Client Application
    participant Controller as AuthController
    participant Service as AuthenticationService
    participant Keycloak as Keycloak IDP
    participant DB as User Database
    
    User->>Client: Enter email & password
    Client->>Controller: POST /api/auth/login<br/>{email, password}
    
    Controller->>Service: login(LoginRequest)
    
    Note over Service: Build LoginParam with<br/>grant_type=password
    Service->>Keycloak: POST /realms/{realm}/protocol/openid-connect/token<br/>(username, password, client_id, client_secret)
    
    alt Authentication Successful
        Keycloak-->>Service: LoginExchangeResponse<br/>(access_token, refresh_token)
        
        Note over Service: Decode JWT token
        Service->>Service: JWT.decode(accessToken)
        Service->>Service: Check email_verified claim
        
        alt Email Not Verified
            Service-->>Controller: throw EMAIL_NOT_VERIFIED
            Controller-->>Client: 400 Bad Request
            Client-->>User: Email not verified error
        else Email Verified
            Service->>DB: findByEmail(email)
            
            alt User Not Found
                DB-->>Service: User not found
                Service-->>Controller: throw NOT_FOUND
                Controller-->>Client: 404 Not Found
                Client-->>User: User not found error
            else User Found
                DB-->>Service: User entity
                
                alt User Inactive
                    Service-->>Controller: throw USER_INACTIVE
                    Controller-->>Client: 403 Forbidden
                    Client-->>User: Account inactive error
                else User Active
                    Service-->>Controller: LoginResponse<br/>(accessToken, refreshToken)
                    Controller-->>Client: 200 OK<br/>ApiResponse<LoginResponse>
                    Client-->>User: Login successful
                end
            end
        end
    else Authentication Failed
        Keycloak-->>Service: FeignException (401/400)
        Service->>Service: errorNormalizer.handleKeyCloakException()
        Service-->>Controller: throw AppException
        Controller-->>Client: Error Response
        Client-->>User: Invalid credentials
    end
```

## 2. Google OAuth Login Flow

```mermaid
sequenceDiagram
    actor User
    participant Client as Client Application
    participant Controller as AuthController
    participant Service as AuthenticationService
    participant Keycloak as Keycloak IDP
    participant Google as Google OAuth
    participant DB as User Database
    participant Wallet as WalletService
    
    User->>Client: Click "Login with Google"
    Client->>Google: Redirect to Google OAuth
    Google->>User: Google Login Page
    User->>Google: Enter credentials & authorize
    Google-->>Client: Authorization code + redirect_uri
    
    Client->>Controller: POST /api/auth/login-google<br/>{code, redirectUri}
    Controller->>Service: loginWithGoogle(LoginGoogleRequest)
    
    Note over Service: Build GoogleLoginParam with<br/>grant_type=authorization_code
    Service->>Keycloak: POST /realms/{realm}/protocol/openid-connect/token<br/>(code, redirect_uri, client_id, client_secret)
    
    Keycloak->>Google: Validate authorization code
    Google-->>Keycloak: User info
    
    alt Token Exchange Successful
        Keycloak-->>Service: LoginExchangeResponse<br/>(access_token, refresh_token, id_token)
        
        Note over Service: Decode ID token
        Service->>Service: JWT.decode(idToken)
        Service->>Service: Extract claims:<br/>- sub (keycloakId)<br/>- email<br/>- given_name<br/>- family_name
        
        alt Missing Required Claims
            Service-->>Controller: throw INVALID_TOKEN
            Controller-->>Client: 400 Bad Request
            Client-->>User: Invalid token error
        else Valid Claims
            Service->>DB: findByEmail(email)
            
            alt User Not Found (First Time Login)
                DB-->>Service: null
                
                Note over Service: Create new user
                Service->>Service: Build User entity<br/>(keycloakId, email, firstName, lastName,<br/>role=CUSTOMER, isActive=true)
                Service->>DB: save(newUser)
                DB-->>Service: Saved user
                
                Service->>Wallet: createWallet(userId)
                
                alt Wallet Creation Success
                    Wallet-->>Service: Wallet created
                else Wallet Creation Failed
                    Wallet-->>Service: Exception (logged, continue)
                end
            else User Exists
                DB-->>Service: User entity
            end
            
            alt User Inactive
                Service-->>Controller: throw USER_INACTIVE
                Controller-->>Client: 403 Forbidden
                Client-->>User: Account inactive error
            else User Active
                Service-->>Controller: LoginResponse<br/>(accessToken, refreshToken)
                Controller-->>Client: 200 OK<br/>ApiResponse<LoginResponse>
                Client-->>User: Login successful
            end
        end
    else Token Exchange Failed
        Keycloak-->>Service: FeignException
        Service->>Service: errorNormalizer.handleKeyCloakException()
        Service-->>Controller: throw AppException
        Controller-->>Client: Error Response
        Client-->>User: Google login failed
    end
```

## 3. Refresh Token Flow

```mermaid
sequenceDiagram
    actor User
    participant Client as Client Application
    participant Controller as AuthController
    participant Service as AuthenticationService
    participant Keycloak as Keycloak IDP
    
    Note over Client: Access token expired
    User->>Client: Request protected resource
    Client->>Client: Detect token expiration
    
    Client->>Controller: POST /api/auth/refresh-token<br/>{refreshToken}
    Controller->>Service: refreshToken(TokenRequest)
    
    Note over Service: Build RefreshTokenParam with<br/>grant_type=refresh_token
    Service->>Keycloak: POST /realms/{realm}/protocol/openid-connect/token<br/>(refresh_token, client_id, client_secret)
    
    alt Refresh Token Valid
        Keycloak-->>Service: LoginExchangeResponse<br/>(new access_token, new refresh_token)
        Service-->>Controller: LoginResponse<br/>(accessToken, refreshToken)
        Controller-->>Client: 200 OK<br/>ApiResponse<LoginResponse>
        Client->>Client: Store new tokens
        Client-->>User: Continue with request
    else Refresh Token Invalid/Expired
        Keycloak-->>Service: FeignException (401)
        Service->>Service: errorNormalizer.handleKeyCloakException()
        Service-->>Controller: throw AppException
        Controller-->>Client: Error Response
        Client-->>User: Session expired, please login
    end
```

## 4. User Registration Flow

```mermaid
sequenceDiagram
    actor User
    participant Client as Client Application
    participant Controller as AuthController
    participant Service as AuthenticationService
    participant Keycloak as Keycloak IDP
    participant DB as User Database
    participant Wallet as WalletService
    
    User->>Client: Fill registration form<br/>(email, password, firstName, lastName, avatarUrl)
    Client->>Client: Validate input<br/>(email format, password min 6 chars)
    
    Client->>Controller: POST /api/auth/register<br/>RegisterRequest
    Controller->>Controller: Validate @Valid annotations
    
    alt Validation Failed
        Controller-->>Client: 400 Bad Request<br/>(Validation errors)
        Client-->>User: Show validation errors
    else Validation Passed
        Controller->>Service: register(RegisterRequest)
        
        Note over Service: Step 1: Get Admin Token
        Service->>Keycloak: POST /realms/{realm}/protocol/openid-connect/token<br/>(grant_type=client_credentials,<br/>client_id, client_secret)
        
        alt Token Exchange Failed
            Keycloak-->>Service: FeignException
            Service->>Service: errorNormalizer.handleKeyCloakException()
            Service-->>Controller: throw AppException
            Controller-->>Client: Error Response
            Client-->>User: Registration failed
        else Token Exchange Success
            Keycloak-->>Service: TokenExchangeResponse<br/>(admin access_token)
            
            Note over Service: Step 2: Create User in Keycloak
            Service->>Service: Build UserCreationParam<br/>(username=email, firstName, lastName,<br/>email, enabled=true,<br/>emailVerified=false,<br/>credentials=[password])
            
            Service->>Keycloak: POST /admin/realms/{realm}/users<br/>(Bearer token, UserCreationParam)
            
            alt User Already Exists
                Keycloak-->>Service: FeignException (409 Conflict)
                Service->>Service: errorNormalizer.handleKeyCloakException()
                Service-->>Controller: throw AppException<br/>(USER_ALREADY_EXISTS)
                Controller-->>Client: 409 Conflict
                Client-->>User: Email already registered
            else User Creation Success
                Keycloak-->>Service: 201 Created<br/>(Location header with userId)
                
                Note over Service: Step 3: Extract Keycloak User ID
                Service->>Service: extractUserId(response)<br/>(parse Location header)
                
                Note over Service: Step 4: Create User in Database
                Service->>Service: Build User entity<br/>(keycloakId, email, firstName,<br/>lastName, role=CUSTOMER,<br/>isActive=true, avatarUrl,<br/>createAt=now())
                
                Service->>DB: save(user)
                
                alt Database Save Failed
                    DB-->>Service: Exception
                    Note over Service: User created in Keycloak<br/>but not in DB (inconsistent state)
                    Service-->>Controller: throw Exception
                    Controller-->>Client: 500 Internal Server Error
                    Client-->>User: Registration failed
                else Database Save Success
                    DB-->>Service: Saved User entity
                    
                    Note over Service: Step 5: Create Wallet
                    Service->>Wallet: createWallet(userId)
                    
                    alt Wallet Creation Failed
                        Wallet-->>Service: Exception
                        Note over Service: User created but wallet failed<br/>(inconsistent state)
                        Service-->>Controller: throw Exception
                        Controller-->>Client: 500 Internal Server Error
                        Client-->>User: Registration failed
                    else Wallet Creation Success
                        Wallet-->>Service: Wallet created
                        
                        Service-->>Controller: void (success)
                        Controller-->>Client: 200 OK<br/>ApiResponse("Register successful")
                        Client-->>User: Registration successful!<br/>Please verify your email
                        
                        Note over User: User should receive<br/>verification email<br/>(if configured in Keycloak)
                    end
                end
            end
        end
    end
```

## Key Components

### Controllers
- **AuthController**: REST API endpoints for authentication operations

### Services
- **AuthenticationService**: Business logic for authentication
- **ErrorNormalizer**: Handles Keycloak exceptions and converts to AppExceptions
- **WalletService**: Creates wallet for new users (Google OAuth)

### External Systems
- **Keycloak**: Identity Provider (IDP) for authentication and user management
- **Google OAuth**: Third-party authentication provider

### Data Models
- **User**: User entity stored in application database
- **LoginRequest**: Email and password for standard login
- **LoginGoogleRequest**: Authorization code and redirect URI for Google OAuth
- **RegisterRequest**: User registration data (email, password, firstName, lastName, avatarUrl)
- **LoginResponse**: Contains access_token and refresh_token
- **TokenRequest**: Contains refresh_token for token refresh
- **UserCreationParam**: Keycloak user creation parameters
- **TokenExchangeResponse**: Admin token from Keycloak for service-to-service calls

### Security Validations
1. **Login Validations**:
   - Email verification check (standard login)
   - User existence check
   - User active status check
   - JWT token validation
   - Required claims validation (Google OAuth)

2. **Registration Validations**:
   - Email format validation (must be valid email)
   - Email minimum length (4 characters)
   - Password minimum length (6 characters)
   - Non-blank email and password fields
   - Duplicate email check (via Keycloak)

### Registration Process Steps
1. **Get Admin Token**: Service obtains admin access token using client credentials
2. **Create User in Keycloak**: User account created in identity provider with credentials
3. **Extract User ID**: Parse Keycloak user ID from Location header
4. **Save to Database**: Create user record in application database
5. **Create Wallet**: Initialize user wallet for transactions

### Potential Issues & Inconsistent States
⚠️ **Important**: The registration process has potential consistency issues:
- If Keycloak user creation succeeds but database save fails → User exists in Keycloak but not in app DB
- If database save succeeds but wallet creation fails → User exists without a wallet
- **Recommendation**: Implement compensating transactions or use distributed transaction patterns (Saga pattern)

### Error Handling
- `EMAIL_NOT_VERIFIED`: Email not verified in Keycloak (login)
- `NOT_FOUND`: User not found in database (login)
- `USER_INACTIVE`: User account is inactive (login)
- `INVALID_TOKEN`: Invalid or missing JWT claims (Google OAuth)
- `USER_ALREADY_EXISTS`: Email already registered (registration)
- `INVALID_USERNAME`: Email format invalid or too short (registration)
- `INVALID_PASSWORD`: Password too short (registration)
- `FeignException`: Keycloak API errors (handled by ErrorNormalizer)
