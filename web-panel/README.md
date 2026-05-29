# PetAdopt — Веб-панель для приютов

React + TypeScript + Vite + TailwindCSS + Supabase

## Запуск

```bash
cd web-panel
npm install
npm run dev
```

## Настройка

Скопируйте `.env.example` в `.env` и заполните:

```
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your_anon_key_here
```

## Страницы

- **/** — Дашборд со статистикой и графиком заявок
- **/pets** — Список питомцев с фильтрами и поиском
- **/pets/new** — Добавление питомца (фото → Supabase Storage)
- **/pets/:id** — Редактирование питомца
- **/applications** — Список заявок на питомцев приюта
- **/applications/:id** — Детальная заявка с анкетой и оценкой рисков

## Доступ

Только пользователи с ролью `shelter` или `admin` в таблице `users`.

## Сборка

```bash
npm run build
```

Выход в `dist/`, готов к деплою на Vercel/Netlify.
