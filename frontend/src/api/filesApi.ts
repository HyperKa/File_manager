import axios from 'axios';
import type {FileResponse} from '../types/file';

const api = axios.create({
    baseURL: '/api/v1', // Прокси автоматически добавит http://localhost:8080
});

export const filesApi = {
    getAllFiles: async (): Promise<FileResponse[]> => {
        const response = await api.get<{
            content: FileResponse[];
            totalElements: number;
            totalPages: number;
        }>('/files');
        return response.data.content; // <-- Берем массив из поля content
    },

    uploadFile: async (file: File): Promise<FileResponse> => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post<FileResponse>('/files', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return response.data;
    },

    getFile: (fileId: string): string => {
        return `/api/v1/files/${fileId}/content`;
    },

    deleteFile: async (fileId: string): Promise<void> => {
        await api.delete(`/files/${fileId}`);
    },

    renameFile: async (fileId: string, newName: string): Promise<FileResponse> => {
        const response = await api.patch<FileResponse>(`/files/${fileId}`, { newName });
        return response.data;
    },
};