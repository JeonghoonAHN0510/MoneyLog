import { API_BASE_URL } from '../api/axiosConfig';

export function buildProfileImageViewUrl(profileImageUrl?: string | null) {
    if (!profileImageUrl) {
        return null;
    }

    return `${API_BASE_URL}/files/view?fileUrl=${encodeURIComponent(profileImageUrl)}`;
}

export function getProfileInitial(name?: string) {
    if (!name) {
        return 'U';
    }
    return name.trim().charAt(0).toUpperCase();
}
