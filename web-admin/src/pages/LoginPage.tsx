
import { useState, type FormEvent } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import "./LoginPage.css";

function getLoginError(error: unknown): string {
    const code =
        typeof error === "object" &&
            error !== null &&
            "code" in error
            ? String(error.code)
            : "";

    switch (code) {
        case "auth/invalid-credential":
        case "auth/wrong-password":
        case "auth/user-not-found":
            return "The email or password is incorrect.";

        case "auth/invalid-email":
            return "The email address is invalid.";

        case "auth/popup-closed-by-user":
            return "The Google sign-in window was closed.";

        case "auth/popup-blocked":
            return "The browser blocked the sign-in popup. Allow popups and try again.";

        case "auth/too-many-requests":
            return "Too many sign-in attempts. Please try again later.";

        default:
            return error instanceof Error
                ? error.message
                : "Sign-in failed. Please try again.";
    }
}

export default function LoginPage() {

    const {
        account,
        error: authError,
        signInWithEmail,
        signInWithGoogle,
        clearError,
    } = useAuth();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [busy, setBusy] = useState(false);
    const [loginError, setLoginError] = useState("");

    if (account) {
        return <Navigate to="/" replace />;
    }

    async function handleEmailLogin(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setBusy(true);
        setLoginError("");
        clearError();

        try {
            await signInWithEmail(email, password);
        } catch (error) {
            setLoginError(getLoginError(error));
        } finally {
            setBusy(false);
        }
    }

    async function handleGoogleLogin() {
        setBusy(true);
        setLoginError("");
        clearError();

        try {
            await signInWithGoogle();
        } catch (error) {
            setLoginError(getLoginError(error));
        } finally {
            setBusy(false);
        }
    }

    const displayedError = loginError || authError;

    return (
        <main className="login-page">
            <section className="login-card">
                <div className="login-brand">
                    <div className="login-brand-icon">R</div>
                    <span>Room Rental</span>
                </div>

                <div className="login-heading">
                    <h1>Welcome back</h1>
                    <p>Sign in to access the administration panel.</p>
                </div>

                {displayedError && (
                    <div className="login-error" role="alert">
                        {displayedError}
                    </div>
                )}

                <form onSubmit={handleEmailLogin} className="login-form">
                    <label htmlFor="login-email">Email address</label>
                    <input
                        id="login-email"
                        type="email"
                        placeholder="admin@example.com"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        autoComplete="username"
                        required
                    />

                    <label htmlFor="login-password">Password</label>
                    <input
                        id="login-password"
                        type="password"
                        placeholder="Enter your password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        autoComplete="current-password"
                        required
                    />

                    <button
                        type="submit"
                        className="login-submit"
                        disabled={busy}
                    >
                        {busy ? "Signing in..." : "Sign in"}
                    </button>
                </form>

                <div className="login-divider">
                    <span>OR</span>
                </div>

                <button
                    type="button"
                    className="login-google"
                    onClick={handleGoogleLogin}
                    disabled={busy}
                >
                    <span className="google-letter">G</span>
                    Continue with Google
                </button>

                <p className="login-footer">
                    Authorized administrators only.
                </p>
            </section>
        </main>
    );
}
