import { useEffect, useState } from 'react';
import toast, { Toaster } from 'react-hot-toast'; // Всплывающие уведомления

// Импортируем наши созданные компоненты и API
import { Header } from './components/Header';
import { FileList } from './components/FileList';
import { UploadButton } from './components/UploadButton';
import { filesApi } from './api/filesApi';
import type { FileResponse } from './types/file';

import './index.css'; // Наши стили Opera GX

function App() {
    // 1. СОСТОЯНИЕ (State): Здесь хранятся наши данные, как private List<File> в Java
    const [files, setFiles] = useState<FileResponse[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);

    // 2. МЕТОД ЗАГРУЗКИ: Аналог @PostConstruct или init()
    const loadFiles = async () => {
        try {
            setIsLoading(true);
            const data = await filesApi.getAllFiles(); // Запрос к бэкенду
            setFiles(data); // Обновляем состояние. Это триггерит перерисовку!
        } catch (error) {
            console.error('Ошибка загрузки:', error);
            toast.error('Не удалось подключиться к серверу');
        } finally {
            setIsLoading(false);
        }
    };

    // 3. ЭФФЕКТ: Выполняется один раз при запуске приложения
    useEffect(() => {
        loadFiles();
    }, []);

    // 4. ОТРИСОВКА (View): Собираем всё воедино
    return (
        <div className="container">
            <Toaster position="top-right" />

            <Header />

            <main>
                {/* Кнопка загрузки. При успехе она вызовет loadFiles */}
                <UploadButton onUploadSuccess={loadFiles} />

                {isLoading ? (
                    <div className="loading-state">Загрузка данных...</div>
                ) : (
                    /* Таблица. Мы передаем ей массив files И функцию loadFiles под именем onRefresh */
                    <FileList files={files} onRefresh={loadFiles} />
                )}
            </main>
        </div>
    );
}

export default App;