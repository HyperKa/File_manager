import React, { useState, useEffect } from 'react';

interface FileItem {
    id: string;
    fileName: string;
    mediaType: string;
    sizeBytes: number;
    status: string;
    uploadedAt: string;
    downloadUrl: string;
}

export default function App() {
    const [token, setToken] = useState<string>(localStorage.getItem('api_token') || 'local-dev-token');
    const [files, setFiles] = useState<FileItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);

    useEffect(() => {
        localStorage.setItem('api_token', token);
        fetchFiles();
    }, [token]);

    const authHeaders = {
        'Authorization': `Bearer ${token}`
    };

    // READ (List)
    const fetchFiles = async () => {
        try {
            setLoading(true);
            const res = await fetch('/api/v1/files?page=0&size=50', { headers: authHeaders });
            if (res.ok) {
                const data = await res.json();
                setFiles(data.content || []);
            }
        } catch (err) {
            console.error('Ошибка загрузки списка:', err);
        } finally {
            setLoading(false);
        }
    };

    // CREATE (Upload)
    const handleUpload = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedFile) return;

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            setUploading(true);
            const res = await fetch('/api/v1/files', {
                method: 'POST',
                headers: authHeaders,
                body: formData
            });
            if (res.ok) {
                setSelectedFile(null);
                // Сбросить инпут
                (document.getElementById('file-input') as HTMLInputElement).value = '';
                await fetchFiles();
            } else {
                alert('Ошибка при загрузке файла');
            }
        } catch (err) {
            alert('Сетевая ошибка при загрузке');
        } finally {
            setUploading(false);
        }
    };

    // READ (Download)
    const handleDownload = async (file: FileItem) => {
        try {
            const res = await fetch(file.downloadUrl, { headers: authHeaders });
            if (!res.ok) throw new Error('Download failed');
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = file.fileName;
            document.body.appendChild(a);
            a.click();
            a.remove();
        } catch (err) {
            alert('Не удалось скачать файл');
        }
    };

    // UPDATE (Rename)
    const handleRename = async (id: string, oldName: string) => {
        const newName = prompt('Введите новое имя файла:', oldName);
        if (!newName || newName.trim() === '' || newName === oldName) return;

        try {
            const res = await fetch(`/api/v1/files/${id}`, {
                method: 'PATCH',
                headers: {
                    ...authHeaders,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ newName: newName.trim() })
            });
            if (res.ok) {
                await fetchFiles();
            } else {
                alert('Ошибка при переименовании');
            }
        } catch (err) {
            alert('Сетевая ошибка при переименовании');
        }
    };

    // DELETE
    const handleDelete = async (id: string) => {
        if (!confirm('Вы уверены, что хотите удалить файл?')) return;
        try {
            const res = await fetch(`/api/v1/files/${id}`, {
                method: 'DELETE',
                headers: authHeaders
            });
            if (res.ok) {
                await fetchFiles();
            } else {
                alert('Ошибка при удалении');
            }
        } catch (err) {
            alert('Сетевая ошибка при удалении');
        }
    };

    const formatSize = (bytes: number) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    };

    return (
        <div className="min-h-screen bg-zinc-950 text-white font-sans p-6 md:p-12">
            <div className="max-w-5xl mx-auto space-y-8">

                {/* Хедер и токен */}
                <header className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-b border-zinc-800 pb-6">
                    <div>
                        <h1 className="text-3xl font-extrabold tracking-tight text-white flex items-center gap-2">
                            <span className="text-amber-400">⚡</span> Cloud Storage
                        </h1>
                        <p className="text-sm text-zinc-400 mt-1">Клиент-серверное файловое хранилище (S3/B2)</p>
                    </div>
                    <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-3 py-1.5 rounded-lg w-full md:w-auto">
                        <span className="text-xs text-zinc-500 font-mono">TOKEN:</span>
                        <input
                            type="text"
                            value={token}
                            onChange={(e) => setToken(e.target.value)}
                            className="bg-transparent text-sm text-amber-400 font-mono focus:outline-none w-full md:w-48"
                            placeholder="Bearer Token"
                        />
                    </div>
                </header>

                {/* CREATE: Форма загрузки файла */}
                <section className="bg-zinc-900 border border-zinc-800 rounded-xl p-6 shadow-sm">
                    <h2 className="text-lg font-semibold text-zinc-200 mb-4">Загрузить новый документ</h2>
                    <form onSubmit={handleUpload} className="flex flex-col sm:flex-row items-center gap-4">
                        <input
                            id="file-input"
                            type="file"
                            onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                            className="block w-full text-sm text-zinc-400 file:mr-4 file:py-2.5 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-zinc-800 file:text-zinc-200 hover:file:bg-zinc-700 cursor-pointer"
                        />
                        <button
                            type="submit"
                            disabled={!selectedFile || uploading}
                            className="w-full sm:w-auto whitespace-nowrap bg-amber-400 hover:bg-amber-300 disabled:opacity-50 text-zinc-950 font-bold px-6 py-2.5 rounded-lg transition-colors cursor-pointer"
                        >
                            {uploading ? 'Загрузка...' : 'Загрузить (POST)'}
                        </button>
                    </form>
                </section>

                {/* READ, UPDATE, DELETE: Список файлов */}
                <section className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden shadow-sm">
                    <div className="p-5 border-b border-zinc-800 flex justify-between items-center">
                        <h2 className="text-lg font-semibold text-zinc-200">Файлы в хранилище</h2>
                        <button
                            onClick={fetchFiles}
                            className="text-xs bg-zinc-800 hover:bg-zinc-700 text-zinc-300 px-3 py-1.5 rounded transition cursor-pointer"
                        >
                            Обновить список
                        </button>
                    </div>

                    {loading ? (
                        <div className="p-12 text-center text-zinc-500">Загрузка данных...</div>
                    ) : files.length === 0 ? (
                        <div className="p-12 text-center text-zinc-500">Нет сохраненных файлов.</div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left border-collapse">
                                <thead>
                                <tr className="border-b border-zinc-800 bg-zinc-900/50 text-xs text-zinc-400 uppercase tracking-wider">
                                    <th className="py-3 px-4">Имя файла</th>
                                    <th className="py-3 px-4">Размер</th>
                                    <th className="py-3 px-4">Статус</th>
                                    <th className="py-3 px-4">Загружен</th>
                                    <th className="py-3 px-4 text-right">Действия (CRUD)</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-zinc-800 text-sm">
                                {files.map((file) => (
                                    <tr key={file.id} className="hover:bg-zinc-800/40 transition">
                                        <td className="py-3.5 px-4 font-medium text-white max-w-xs truncate" title={file.fileName}>
                                            {file.fileName}
                                        </td>
                                        <td className="py-3.5 px-4 text-zinc-400 font-mono text-xs">{formatSize(file.sizeBytes)}</td>
                                        <td className="py-3.5 px-4">
                        <span className={`inline-block px-2 py-0.5 text-xs rounded font-mono ${
                            file.status === 'READY' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-amber-950 text-amber-400 border border-amber-800'
                        }`}>
                          {file.status}
                        </span>
                                        </td>
                                        <td className="py-3.5 px-4 text-zinc-400 text-xs">
                                            {new Date(file.uploadedAt).toLocaleString()}
                                        </td>
                                        <td className="py-3.5 px-4 text-right space-x-2 whitespace-nowrap">
                                            <button
                                                onClick={() => handleDownload(file)}
                                                className="px-2.5 py-1 text-xs rounded bg-zinc-800 hover:bg-zinc-700 text-zinc-200 border border-zinc-700 transition cursor-pointer"
                                                title="Скачать (GET)"
                                            >
                                                Скачать
                                            </button>
                                            <button
                                                onClick={() => handleRename(file.id, file.fileName)}
                                                className="px-2.5 py-1 text-xs rounded bg-amber-400/10 hover:bg-amber-400/20 text-amber-300 border border-amber-500/30 transition cursor-pointer"
                                                title="Переименовать (PATCH)"
                                            >
                                                Переименовать
                                            </button>
                                            <button
                                                onClick={() => handleDelete(file.id)}
                                                className="px-2.5 py-1 text-xs rounded bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 border border-rose-800/50 transition cursor-pointer"
                                                title="Удалить (DELETE)"
                                            >
                                                Удалить
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>

            </div>
        </div>
    );
}