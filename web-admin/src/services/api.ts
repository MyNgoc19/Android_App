
const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8000/api/v1"
).replace(/\/+$/, "");

type AccessTokenProvider = () => Promise<string | null>;

let accessTokenProvider: AccessTokenProvider | null = null;

export function setAccessTokenProvider(
    provider: AccessTokenProvider | null,
) {
    accessTokenProvider = provider;
}

export class ApiError extends Error {
    status: number;
    detail: unknown;

    constructor(status: number, message: string, detail?: unknown) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.detail = detail;
    }
}

interface ApiRequestOptions extends Omit<RequestInit, "body"> {
    body?: unknown;
    auth?: boolean;
}

export async function apiRequest<T>(
    endpoint: string,
    options: ApiRequestOptions = {},
): Promise<T> {
    const {
        body,
        auth = true,
        headers: customHeaders,
        ...fetchOptions
    } = options;

    const headers = new Headers(customHeaders);

    let requestBody: BodyInit | undefined;

    if (body !== undefined && body !== null) {
        if (body instanceof FormData) {
            requestBody = body;
        } else if (
            typeof body === "string" ||
            body instanceof Blob ||
            body instanceof ArrayBuffer
        ) {
            requestBody = body as BodyInit;
        } else {
            headers.set("Content-Type", "application/json");
            requestBody = JSON.stringify(body);
        }
    }

    if (auth && accessTokenProvider) {
        const token = await accessTokenProvider();

        if (token) {
            headers.set("Authorization", `Bearer ${token}`);
        }
    }

    const normalizedEndpoint = endpoint.replace(/^\/+/, "");

    const response = await fetch(
        `${API_BASE_URL}/${normalizedEndpoint}`,
        {
            ...fetchOptions,
            headers,
            body: requestBody,
        },
    );

    if (response.status === 204) {
        return undefined as T;
    }

    const contentType = response.headers.get("content-type");
    let responseData: unknown = null;

    if (contentType?.includes("application/json")) {
        responseData = await response.json();
    } else {
        responseData = (await response.text()) || null;
    }

    if (!response.ok) {
        const detail =
            typeof responseData === "object" &&
                responseData !== null &&
                "detail" in responseData
                ? responseData.detail
                : responseData;

        const message =
            typeof detail === "string"
                ? detail
                : `API request failed with status ${response.status}`;

        throw new ApiError(response.status, message, detail);
    }

    return responseData as T;
}

export function apiGet<T>(
    endpoint: string,
    options?: Omit<ApiRequestOptions, "method" | "body">,
) {
    return apiRequest<T>(endpoint, {
        ...options,
        method: "GET",
    });
}

export function apiPost<T>(
    endpoint: string,
    body?: unknown,
    options?: Omit<ApiRequestOptions, "method" | "body">,
) {
    return apiRequest<T>(endpoint, {
        ...options,
        method: "POST",
        body,
    });
}

export function apiPatch<T>(
    endpoint: string,
    body?: unknown,
    options?: Omit<ApiRequestOptions, "method" | "body">,
) {
    return apiRequest<T>(endpoint, {
        ...options,
        method: "PATCH",
        body,
    });
}

export function apiDelete<T>(
    endpoint: string,
    options?: Omit<ApiRequestOptions, "method">,
) {
    return apiRequest<T>(endpoint, {
        ...options,
        method: "DELETE",
    });
}
