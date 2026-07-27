import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import App from './App';

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={
                    <ProtectedRoute>
                        <App />
                    </ProtectedRoute>
                } />
            </Routes>
        </BrowserRouter>
    );
}