import React from 'react';
import type { FileResponse } from '../types/file';
import { filesApi } from '../api/filesApi';
import toast from 'react-hot-toast';

interface FileListProps {
    files: FileResponse[];
    onRefresh: () => void;
}

export const FileList: React.FC<FileListProps> = ({ files, onRefresh }) => {

    // 1. Обработчик скачивания
    const handleDownload = (file: FileResponse) => {
        // Открытие ссылки в новой вкладке. Браузер сам скачает файл благодаря заголовку Content-Disposition
        window.open(filesApi.getFile(file.id), '_blank');
    };

    // 2. Обработчик удаления
    const handleDelete = async (file: FileResponse) => {
        if (window.confirm(`Вы уверены, что хотите удалить файл "${file.fileName}"?`)) {
            try {
                await filesApi.deleteFile(file.id);
                toast.success('Файл успешно удален');
                onRefresh(); // Обновление списка на экране
            } catch (error) {
                toast.error('Ошибка при удалении файла');
            }
        }
    };

    // 3. Обработчик переименования
    const handleRename = async (file: FileResponse) => {
        const newName = window.prompt('Введите новое имя файла:', file.fileName);
        if (newName && newName.trim() !== '' && newName !== file.fileName) {
            try {
                await filesApi.renameFile(file.id, newName.trim());
                toast.success('Файл переименован');
                onRefresh(); // Обновляем список на экране
            } catch (error) {
                toast.error('Ошибка при переименовании (возможно, имя занято или невалидно)');
            }
        }
    };

    // Вспомогательная функция для красивого размера файла
    const formatSize = (bytes: number): string => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    // Вспомогательная функция для даты
    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleString('ru-RU');
    };

    // Если файлов нет, демонстрация заглушки
    if (!Array.isArray(files) || files.length === 0) {
        return (
            <div className="empty-state">
                <h2>Список файлов пуст</h2>
                <p>Загрузите ваш первый файл, чтобы начать работу</p>
            </div>
        );
    }

    // Основная отрисовка таблицы
    return (
        <table className="file-table">
            <thead>
            <tr>
                <th>Имя файла</th>
                <th>Размер</th>
                <th>Статус</th>
                <th>Дата изменения</th>
                <th style={{ textAlign: 'center' }}>Действия</th>
            </tr>
            </thead>
            <tbody>
            {Array.isArray(files) && files.map((file) => (
                <tr key={file.id}>
                    <td>{file.fileName}</td>
                    <td>{formatSize(file.sizeBytes)}</td>
                    <td>
              <span className={`status-badge status-${file.status.toLowerCase()}`}>
                {file.status === 'READY' ? '✅ Готов' : file.status === 'PENDING' ? '⏳ Загрузка' : '❌ Ошибка'}
              </span>
                    </td>
                    <td>{formatDate(file.updatedAt)}</td>
                    <td className="actions-cell">
                        <button className="icon-btn" onClick={() => handleDownload(file)} title="Скачать">⬇️</button>
                        <button className="icon-btn" onClick={() => handleRename(file)} title="Переименовать">✏️</button>
                        <button className="icon-btn danger" onClick={() => handleDelete(file)} title="Удалить">🗑️</button>
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};