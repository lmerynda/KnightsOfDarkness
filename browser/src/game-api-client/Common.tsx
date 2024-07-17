type RequestFunction = () => Promise<Response>;

const redirectToLogin = (): never => {
    window.location.href = '/login';
    throw new Error('Authentication error, redirecting to login');
};

export const handleResponse = async (requestFunction: RequestFunction): Promise<Response> => {
    try {
        const response = await requestFunction();
        if (response.status === 401) {
            redirectToLogin();
        }
        return response;
    } catch (error) {
        throw error;
    }
};

export const fetchData = (url: string, options: RequestInit): RequestFunction => () => fetch(url, options);

export const handlePostRequest = (url: string, data: any): Promise<Response> => handleResponse(() =>
    fetchData(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })()
);