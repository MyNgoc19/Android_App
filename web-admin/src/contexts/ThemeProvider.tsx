
import {
    useEffect,
    useState,
    useCallback,
    type ReactNode,
} from "react";

import { ThemeContext, type Theme } from "./ThemeContext";

const getInitialTheme = (): Theme => {
    try {
        const savedTheme = localStorage.getItem("admin-theme");

        return savedTheme === "dark" ? "dark" : "light";
    } catch {
        return "light";
    }
};

interface ThemeProviderProps {
    children: ReactNode;
}

export function ThemeProvider({ children }: ThemeProviderProps) {
    const [theme, setTheme] = useState<Theme>(getInitialTheme);

    useEffect(() => {
        document.documentElement.setAttribute("data-theme", theme);

        try {
            localStorage.setItem("admin-theme", theme);
        } catch {
            // Ignore storage errors.
        }
    }, [theme]);

    const toggleTheme = useCallback(() => {
        setTheme((currentTheme) =>
            currentTheme === "light" ? "dark" : "light"
        );
    }, []);

    return (
        <ThemeContext.Provider value={{ theme, toggleTheme }}>
            {children}
        </ThemeContext.Provider>
    );
}
