
import {
    createContext,
    useCallback,
    useEffect,
    useState,
    type ReactNode,
} from "react";

import {
    onAuthStateChanged,
    signInWithEmailAndPassword,
    signInWithPopup,
    GoogleAuthProvider,
    signOut,
    type User,
} from "firebase/auth";

import { auth } from "../config/firebase";
import { apiGet, apiPost, setAccessTokenProvider } from "../services/api";

export interface AdminAccount {
    id: number;
    firebase_uid: string;
    email: string;
    full_name: string;
    phone: string | null;
    avatar_url: string | null;
    role: "USER" | "ADMIN";
    status: "ACTIVE" | "BLOCKED";
}

interface AuthContextValue {
    user: User | null;
    account: AdminAccount | null;
    loading: boolean;
    error: string | null;
    signInWithEmail: (email: string, password: string) => Promise<void>;
    signInWithGoogle: () => Promise<void>;
    logout: () => Promise<void>;
    clearError: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(
    undefined,
);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(null);
    const [account, setAccount] = useState<AdminAccount | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);


    useEffect(() => {
        if (!auth) {
            setError("Firebase Web chưa được cấu hình.");
            setLoading(false);
            return;
        }

        const firebaseAuth = auth;

        setAccessTokenProvider(async () => {
            return firebaseAuth.currentUser
                ? firebaseAuth.currentUser.getIdToken()
                : null;
        });

        const unsubscribe = onAuthStateChanged(
            firebaseAuth,
            async (firebaseUser) => {
                if (!firebaseUser) {
                    setAccount(null);
                    setLoading(false);
                    return;
                }

                try {
                    await apiPost("auth/session");

                    const currentAccount =
                        await apiGet<AdminAccount>("auth/me");

                    if (
                        currentAccount.role !== "ADMIN" ||
                        currentAccount.status !== "ACTIVE"
                    ) {
                        throw new Error(
                            "Tài khoản không có quyền truy cập Admin."
                        );
                    }

                    setAccount(currentAccount);
                    setError(null);
                } catch (error) {
                    setAccount(null);
                    setError(
                        error instanceof Error
                            ? error.message
                            : "Không thể xác thực tài khoản."
                    );

                    await signOut(firebaseAuth);
                } finally {
                    setLoading(false);
                }
            }
        );

        return unsubscribe;
    }, []);


    const signInWithEmail = useCallback(
        async (email: string, password: string) => {
            if (!auth) {
                throw new Error("Firebase Web chưa được cấu hình.");
            }

            setError(null);

            await signInWithEmailAndPassword(
                auth,
                email.trim(),
                password,
            );
        },
        [],
    );

    const signInWithGoogle = useCallback(async () => {
        if (!auth) {
            throw new Error("Firebase Web chưa được cấu hình.");
        }

        setError(null);

        const provider = new GoogleAuthProvider();
        provider.setCustomParameters({
            prompt: "select_account",
        });

        await signInWithPopup(auth, provider);
    }, []);

    const logout = useCallback(async () => {
        if (!auth) return;

        await signOut(auth);
        setAccount(null);
        setUser(null);
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return (
        <AuthContext.Provider
            value={{
                user,
                account,
                loading,
                error,
                signInWithEmail,
                signInWithGoogle,
                logout,
                clearError,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}
