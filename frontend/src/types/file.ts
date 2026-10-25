// Полное отражение FileResponse.java
export interface FileResponse {
    id: string;
    fileName: string;
    mediaType: string;
    sizeBytes: number;
    status: 'PENDING' | 'READY' | 'FAILED';
    updatedAt: string;
    downloadUrl: string;
}