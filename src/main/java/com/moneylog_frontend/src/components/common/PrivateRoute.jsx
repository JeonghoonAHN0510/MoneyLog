import { Navigate } from "react-router-dom";
import { useAuthStore } from "../../stores/authStore";

export default function PrivateRoute({children}){
    // Zustand Store에서 로그인 여부 추출
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

    // 로그인 하지않았으면, 로그인 페이지로 이동
    if (!isAuthenticated){
        return <Navigate to="/login" replace/>;
    }
    // 로그인했으면, 통과
    return children;
} // func end