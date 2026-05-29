import { useState, useRef, useCallback, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { supabase } from '../lib/supabase'
import { uploadToS3 } from '../lib/s3'
import type { PetType, PetGender, PetSize } from '../lib/types'
import { Upload, FileSpreadsheet, FileArchive, Check, AlertCircle, X, ArrowLeft, Loader2 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import * as XLSX from 'xlsx'
import JSZip from 'jszip'

interface ImportRow {
  row: number
  name: string
  age: number
  type: PetType
  gender: PetGender
  size: PetSize
  breed: string
  color: string
  weight: number | null
  traits: string[]
  description: string
  photo_filename: string
  photo_file?: File
  is_neutered: boolean
  has_vaccination: boolean
  is_active: boolean
  errors: string[]
  status: 'pending' | 'uploading' | 'success' | 'error'
}

const VALID_TYPES: PetType[] = ['dog', 'cat', 'bird', 'other']
const VALID_GENDERS: PetGender[] = ['male', 'female']
const VALID_SIZES: PetSize[] = ['small', 'medium', 'large']

export default function PetImportPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [step, setStep] = useState<'upload' | 'preview' | 'importing' | 'done'>('upload')
  const [rows, setRows] = useState<ImportRow[]>([])
  const zipFilesRef = useRef<Map<string, File>>(new Map())
  const [progress, setProgress] = useState({ current: 0, total: 0 })
  const [results, setResults] = useState({ success: 0, errors: 0 })

  const xlsxInputRef = useRef<HTMLInputElement>(null)
  const zipInputRef = useRef<HTMLInputElement>(null)

  function matchPhotos(currentRows: ImportRow[]): ImportRow[] {
    const files = zipFilesRef.current
    if (files.size === 0) return currentRows
    
    console.log('[MatchPhotos] Matching', currentRows.length, 'rows with', files.size, 'files')
    
    return currentRows.map((r) => {
      if (!r.photo_filename || r.photo_file) return r
      const normalizedName = r.photo_filename.toLowerCase().trim()
      let photoFile = files.get(normalizedName)
      if (!photoFile) photoFile = files.get(normalizedName.replace(/\s+/g, '_'))
      if (!photoFile) {
        const noExt = normalizedName.replace(/\.[^/.]+$/, '')
        photoFile = files.get(noExt)
      }
      if (!photoFile) {
        const exts = ['.jpg', '.jpeg', '.png', '.webp']
        for (const ext of exts) {
          photoFile = files.get(normalizedName + ext)
          if (photoFile) break
        }
      }
      if (photoFile) {
        console.log('[MatchPhotos] Matched:', r.name, '->', normalizedName)
        return { ...r, photo_file: photoFile, errors: r.errors.filter((e) => !e.includes('фото')) }
      }
      return r
    })
  }

  function parseBoolean(val: unknown): boolean {
    if (typeof val === 'boolean') return val
    if (typeof val === 'number') return val === 1
    if (typeof val === 'string') {
      const lowered = val.toLowerCase().trim()
      return lowered === 'true' || lowered === 'да' || lowered === '1' || lowered === 'yes'
    }
    return false
  }

  function validateRow(row: Record<string, unknown>, rowNum: number): ImportRow {
    console.log('[ValidateRow]', rowNum, 'raw data:', row)
    const errors: string[] = []

    if (!row.name || String(row.name).trim() === '') errors.push('Имя обязательно')
    if (!row.age || isNaN(Number(row.age)) || Number(row.age) < 0 || Number(row.age) > 30) {
      errors.push('Возраст должен быть числом 0-30')
    }
    const type = String(row.type || '').toLowerCase().trim() as PetType
    if (!VALID_TYPES.includes(type)) errors.push(`Тип должен быть: ${VALID_TYPES.join(', ')}`)

    const gender = String(row.gender || '').toLowerCase().trim() as PetGender
    if (!VALID_GENDERS.includes(gender)) errors.push(`Пол должен быть: male/female`)

    const size = String(row.size || 'medium').toLowerCase().trim() as PetSize
    const finalSize = VALID_SIZES.includes(size) ? size : 'medium'

    const traits = String(row.traits || '')
      .split(/[,;]/)
      .map((s) => s.trim())
      .filter(Boolean)

    const photoFilename = String(row.photo_filename || row.photo || '').trim()

    return {
      row: rowNum,
      name: String(row.name || '').trim(),
      age: Number(row.age) || 0,
      type: VALID_TYPES.includes(type) ? type : 'other',
      gender: VALID_GENDERS.includes(gender) ? gender : 'male',
      size: finalSize,
      breed: String(row.breed || '').trim(),
      color: String(row.color || '').trim(),
      weight: row.weight ? Number(row.weight) : null,
      traits,
      description: String(row.description || '').trim(),
      photo_filename: photoFilename,
      is_neutered: parseBoolean(row.is_neutered),
      has_vaccination: parseBoolean(row.has_vaccination),
      is_active: true,
      errors,
      status: errors.length > 0 ? 'error' : 'pending',
    }
  }

  async function handleXlsxUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    e.target.value = ''

    try {
      const data = await file.arrayBuffer()
      const workbook = XLSX.read(data, { type: 'array' })
      const sheet = workbook.Sheets[workbook.SheetNames[0]]
      const json = XLSX.utils.sheet_to_json(sheet, { header: 1 }) as unknown[][]

      if (json.length < 2) {
        alert('Файл пустой или нет данных')
        return
      }

      const headers = (json[0] as string[]).map((h) => String(h).toLowerCase().trim().replace(/\s+/g, '_'))
      const parsedRows: ImportRow[] = []

      for (let i = 1; i < json.length; i++) {
        const raw = json[i] as unknown[]
        const rowData: Record<string, unknown> = {}
        headers.forEach((h, idx) => {
          rowData[h] = raw[idx]
        })
        parsedRows.push(validateRow(rowData, i + 1))
      }

      setRows(matchPhotos(parsedRows))
      // Не переключаем шаг сразу, даём возможность загрузить ZIP
      console.log('[handleXlsxUpload] Parsed', parsedRows.length, 'rows')
    } catch (err: any) {
      alert('Ошибка чтения Excel: ' + err.message)
    }
  }

  async function handleZipUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    e.target.value = ''

    try {
      const zip = await JSZip.loadAsync(file)
      const files = new Map<string, File>()

      for (const [path, zipEntry] of Object.entries(zip.files)) {
        if (zipEntry.dir) continue
        const name = path.split('/').pop() || path
        const blob = await zipEntry.async('blob')
        const extractedFile = new File([blob], name, { type: blob.type || 'image/jpeg' })
        files.set(name.toLowerCase(), extractedFile)
        files.set(name.toLowerCase().replace(/\s+/g, '_'), extractedFile)
        const nameNoExt = name.toLowerCase().replace(/\.[^/.]+$/, '')
        files.set(nameNoExt, extractedFile)
      }

      zipFilesRef.current = files
      console.log('[handleZipUpload] Loaded', files.size, 'files from ZIP')
      
      if (rows.length > 0) {
        setRows(matchPhotos(rows))
      }
    } catch (err: any) {
      alert('Ошибка чтения ZIP: ' + err.message)
    }
  }

  const startImport = useCallback(async () => {
    if (!user) return
    const validRows = rows.filter((r) => r.errors.length === 0)
    if (validRows.length === 0) return

    setStep('importing')
    setProgress({ current: 0, total: validRows.length })
    setResults({ success: 0, errors: 0 })

    let successCount = 0
    let errorCount = 0

    for (let i = 0; i < validRows.length; i++) {
      const row = validRows[i]
      setProgress({ current: i + 1, total: validRows.length })
      setRows((prev) => prev.map((r) => (r.row === row.row ? { ...r, status: 'uploading' } : r)))

      try {
        let photoUrl = ''
        if (row.photo_file) {
          photoUrl = await uploadToS3(row.photo_file)
        }

        const payload = {
          name: row.name,
          age: row.age,
          type: row.type,
          gender: row.gender,
          size: row.size,
          breed: row.breed,
          color: row.color,
          weight: row.weight,
          traits: row.traits,
          description: row.description,
          photo_url: photoUrl,
          is_neutered: row.is_neutered,
          has_vaccination: row.has_vaccination,
          is_active: row.is_active,
          shelter_id: user.id,
        }

        const { error } = await supabase.from('pets').insert(payload)
        if (error) throw error

        successCount++
        setRows((prev) => prev.map((r) => (r.row === row.row ? { ...r, status: 'success' } : r)))
      } catch (err: any) {
        errorCount++
        setRows((prev) =>
          prev.map((r) =>
            r.row === row.row ? { ...r, status: 'error', errors: [...r.errors, err.message] } : r
          )
        )
      }

      setResults({ success: successCount, errors: errorCount })
    }

    setStep('done')
  }, [rows, user])

  const validCount = rows.filter((r) => r.errors.length === 0).length
  const errorCount = rows.filter((r) => r.errors.length > 0).length

  return (
    <div>
      <button
        onClick={() => navigate('/pets')}
        className="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-4 transition"
      >
        <ArrowLeft className="w-5 h-5" />
        Назад к списку
      </button>

      <h2 className="text-2xl font-bold mb-6">Импорт питомцев</h2>

      {/* Step 1: Upload files */}
      {step === 'upload' && (
        <div className="space-y-6 max-w-xl">
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <FileSpreadsheet className="w-5 h-5 text-green-600" />
              1. Загрузите Excel-файл
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              Файл должен содержать колонки: <code className="bg-gray-100 px-1 rounded">name</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">age</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">type</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">gender</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">size</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">breed</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">color</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">weight</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">traits</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">description</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">photo_filename</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">is_neutered</code>,{' '}
              <code className="bg-gray-100 px-1 rounded">has_vaccination</code>
            </p>
            <button
              onClick={() => xlsxInputRef.current?.click()}
              className={`w-full py-8 border-2 border-dashed rounded-xl transition flex flex-col items-center gap-2 ${
                rows.length > 0
                  ? 'border-green-400 bg-green-50 hover:bg-green-100'
                  : 'border-gray-300 hover:border-primary-500 hover:bg-primary-50'
              }`}
            >
              {rows.length > 0 ? (
                <>
                  <Check className="w-8 h-8 text-green-600" />
                  <span className="text-sm text-green-700 font-medium">Excel загружен ({rows.length} записей)</span>
                </>
              ) : (
                <>
                  <Upload className="w-8 h-8 text-gray-400" />
                  <span className="text-sm text-gray-600">Нажмите или перетащите .xlsx файл</span>
                </>
              )}
            </button>
            <input
              ref={xlsxInputRef}
              type="file"
              accept=".xlsx,.xls,.csv"
              className="hidden"
              onChange={handleXlsxUpload}
            />
            {rows.length > 0 && (
              <button
                onClick={() => {
                  setRows([])
                  xlsxInputRef.current!.value = ''
                }}
                className="w-full mt-3 py-2 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition text-sm"
              >
                Загрузить заново
              </button>
            )}
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <FileArchive className="w-5 h-5 text-amber-600" />
              2. Загрузите ZIP с фото (опционально)
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              Архив с фотографиями. Имена файлов должны совпадать с колонкой{' '}
              <code className="bg-gray-100 px-1 rounded">photo_filename</code> в Excel.
            </p>
            <button
              onClick={() => zipInputRef.current?.click()}
              className={`w-full py-8 border-2 border-dashed rounded-xl transition flex flex-col items-center gap-2 ${
                zipFilesRef.current.size > 0
                  ? 'border-green-400 bg-green-50 hover:bg-green-100'
                  : 'border-gray-300 hover:border-primary-500 hover:bg-primary-50'
              }`}
            >
              {zipFilesRef.current.size > 0 ? (
                <>
                  <Check className="w-8 h-8 text-green-600" />
                  <span className="text-sm text-green-700 font-medium">
                    ZIP загружен ({zipFilesRef.current.size} файлов)
                  </span>
                </>
              ) : (
                <>
                  <Upload className="w-8 h-8 text-gray-400" />
                  <span className="text-sm text-gray-600">Нажмите или перетащите .zip файл</span>
                </>
              )}
            </button>
            <input ref={zipInputRef} type="file" accept=".zip" className="hidden" onChange={handleZipUpload} />
            {zipFilesRef.current.size > 0 && (
              <button
                onClick={() => {
                  zipFilesRef.current = new Map()
                  if (rows.length > 0) {
                    setRows(matchPhotos(rows))
                  }
                }}
                className="w-full mt-3 py-2 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition text-sm"
              >
                Загрузить заново
              </button>
            )}
          </div>

          {rows.length > 0 && (
            <div className="flex justify-center mt-6">
              <button
                onClick={() => setStep('preview')}
                className="px-8 py-3 rounded-xl bg-primary-600 hover:bg-primary-700 text-white font-medium transition flex items-center gap-2"
              >
                Продолжить
                <ArrowLeft className="w-5 h-5 rotate-180" />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Step 2: Preview */}
      {step === 'preview' && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <span className="text-sm">
                Всего: <strong>{rows.length}</strong>
              </span>
              <span className="text-sm text-green-600">
                <Check className="w-4 h-4 inline" /> Корректных: <strong>{validCount}</strong>
              </span>
              {errorCount > 0 && (
                <span className="text-sm text-red-600">
                  <AlertCircle className="w-4 h-4 inline" /> С ошибками: <strong>{errorCount}</strong>
                </span>
              )}
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setRows([])
                  zipFilesRef.current = new Map()
                  setStep('upload')
                }}
                className="px-4 py-2 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
              >
                Назад
              </button>
              <button
                onClick={startImport}
                disabled={validCount === 0}
                className="px-4 py-2 rounded-xl bg-primary-600 hover:bg-primary-700 disabled:bg-primary-300 text-white font-medium transition"
              >
                Импортировать {validCount > 0 ? `(${validCount})` : ''}
              </button>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">#</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Имя</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Возраст</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Тип</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Пол</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Фото</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-600">Статус</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {rows.map((row) => (
                    <tr key={row.row} className={row.errors.length > 0 ? 'bg-red-50' : ''}>
                      <td className="px-4 py-3 text-gray-500">{row.row}</td>
                      <td className="px-4 py-3 font-medium">{row.name || '-'}</td>
                      <td className="px-4 py-3">{row.age || '-'}</td>
                      <td className="px-4 py-3">{row.type}</td>
                      <td className="px-4 py-3">{row.gender}</td>
                      <td className="px-4 py-3">
                        {row.photo_file ? (
                          <span className="text-green-600 flex items-center gap-1">
                            <Check className="w-4 h-4" />
                            {row.photo_filename}
                          </span>
                        ) : row.photo_filename ? (
                          <span className="text-amber-600 flex items-center gap-1">
                            <AlertCircle className="w-4 h-4" />
                            Не найдено
                          </span>
                        ) : (
                          '-'
                        )}
                      </td>
                      <td className="px-4 py-3">
                        {row.errors.length > 0 ? (
                          <div className="flex items-start gap-1 text-red-600">
                            <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />
                            <span className="text-xs">{row.errors.join(', ')}</span>
                          </div>
                        ) : (
                          <span className="text-green-600 flex items-center gap-1">
                            <Check className="w-4 h-4" />
                            Готово
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Step 3: Importing */}
      {step === 'importing' && (
        <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100 text-center max-w-md mx-auto">
          <Loader2 className="w-12 h-12 text-primary-600 animate-spin mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">Импорт...</h3>
          <p className="text-gray-500 mb-4">
            {progress.current} из {progress.total}
          </p>
          <div className="w-full bg-gray-100 rounded-full h-2.5 overflow-hidden">
            <div
              className="bg-primary-600 h-2.5 rounded-full transition-all"
              style={{ width: `${(progress.current / progress.total) * 100}%` }}
            />
          </div>
        </div>
      )}

      {/* Step 4: Done */}
      {step === 'done' && (
        <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100 text-center max-w-md mx-auto">
          <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <Check className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-semibold mb-2">Импорт завершен!</h3>
          <div className="space-y-2 text-sm mb-6">
            <p className="text-green-600">Успешно: <strong>{results.success}</strong></p>
            {results.errors > 0 && <p className="text-red-600">Ошибок: <strong>{results.errors}</strong></p>}
          </div>
          <div className="flex gap-3 justify-center">
            <button
              onClick={() => navigate('/pets')}
              className="px-6 py-2.5 rounded-xl bg-primary-600 hover:bg-primary-700 text-white font-medium transition"
            >
              К списку питомцев
            </button>
            <button
              onClick={() => {
                setRows([])
                zipFilesRef.current = new Map()
                setStep('upload')
              }}
              className="px-6 py-2.5 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
            >
              Новый импорт
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
