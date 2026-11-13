import React, { useRef } from 'react';
import { filesApi } from '../api/filesApi';
import toast from 'react-hot-toast';

// Этот компонент получает только одну функцию: "Что делать после успешной загрузки?"
interface UploadButtonProps {
    onUploadSuccess: () => void;
}

export const UploadButton: React.FC<UploadButtonProps> = ({ onUploadSuccess }) => {
    // 1. useRef - это как указатель (reference) на объект в памяти.
    // создание переменной, которая будет хранить ссылку на реальный HTML-элемент input.
    const fileInputRef = useRef<HTMLInputElement>(null);

    // 2. Обработчик клика по кнопке
    const handleButtonClick = () => {
        // Мы программно нажимаем на скрытый системный input.
        // Аналог в Java: hiddenButton.doClick();
        fileInputRef.current?.click();
    };

    // 3. Обработчик изменения в скрытом input (срабатывает, когда пользователь выбрал файл в окне ОС)
    const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        // event.target.files - это массив выбранных файлов. Берем первый ([0]).
        const file = event.target.files?.[0];
        if (!file) return; // Если пользователь нажал "Отмена", выходим.

        try {
            // Асинхронный вызов API-клиента (аналог RestTemplate.post)
            await filesApi.uploadFile(file);

            // Показываем всплывающее уведомление (Toast)
            toast.success(`Файл "${file.name}" успешно загружен!`);

            // САМОЕ ГЛАВНОЕ: Вызываем коллбек, который пришел из App.tsx
            // Это триггерит loadFiles() в родителе, который обновит таблицу!
            onUploadSuccess();
        } catch (error) {
            toast.error('Ошибка при загрузке файла');
        }

        // Хак для браузеров: если пользователь загрузил файл "test.txt",
        // а потом снова выбрал "test.txt", событие onChange НЕ сработает (браузер думает, что ничего не изменилось).
        // Мы принудительно очищаем значение input, чтобы сбросить это "состояние".
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    return (
        <div style={{ marginBottom: '20px' }}>
            {/* Наша красивая кнопка */}
            <button className="btn-primary" onClick={handleButtonClick}>
                ️ Загрузить новый файл
            </button>

            {/* Скрытый системный input, который делает всю грязную работу */}
            <input
                ref={fileInputRef}
                type="file"
                style={{ display: 'none' }}
                onChange={handleFileChange}
            />
        </div>
    );
};