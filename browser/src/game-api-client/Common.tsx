type RequestFunction = () => Promise<Response>;

const unauthenticateAndRedirectToLogin = (): never => {
  localStorage.removeItem("authToken");
  window.location.href = "/login";
  throw new Error("Authentication error, redirecting to login");
};

export const handleResponse = async (requestFunction: RequestFunction): Promise<Response> => {
  const response = await requestFunction();
  if (response.status === 401) {
    unauthenticateAndRedirectToLogin();
  }
  return response;
};

export const fetchData =
  (url: string, options: RequestInit): RequestFunction =>
  () =>
    fetch(url, options);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const handlePostRequest = (url: string, data: any): Promise<Response> =>
  handleResponse(() =>
    fetchData(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })(),
  );
