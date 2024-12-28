const API_BASE_URL = "http://localhost:3232";

export async function fetchFromAPI(endpoint: string, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
  if (!response.ok) {
    throw new Error(`API call failed: ${response.statusText}`);
  }
  return response.json();
}

export async function getUserListings(userId: string) {
  return fetchFromAPI(`/get-user-listings?seller_id=${userId}`);
}

export async function getUserProfile(userId: string) {
  return fetchFromAPI(`/get-user?clerk_id=${userId}`);
}

export async function updateUserProfile(userId: number, name: string, phoneNumber : string, school: string) {
  return fetchFromAPI(`/update-user?clerk_id=${userId}&name=${name}&phone_number=${phoneNumber}&school=${school}`);
}
