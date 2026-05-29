import { useEffect, useRef, useState } from 'react'
import { supabase } from '../lib/supabase'
import { Send, User } from 'lucide-react'

interface ChatMessage {
  id: string
  application_id: string
  sender_id: string
  sender_role: 'user' | 'shelter' | 'admin'
  message: string
  is_read: boolean
  created_at: string
}

interface ChatProps {
  applicationId: string
  currentUserId: string
}

export default function Chat({ applicationId, currentUserId }: ChatProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    loadMessages()
    
    const channel = supabase.channel(`chat:${applicationId}`)
    
    channel.on(
      'postgres_changes',
      {
        event: 'INSERT',
        schema: 'public',
        table: 'chat_messages',
        filter: `application_id=eq.${applicationId}`,
      },
      (payload) => {
        const newMsg = payload.new as ChatMessage
        setMessages((prev) => [...prev, newMsg])
      }
    )
    
    channel.subscribe((status) => {
      if (status !== 'SUBSCRIBED') {
        console.error('Failed to subscribe:', status)
      }
    })

    return () => {
      supabase.removeChannel(channel)
    }
  }, [applicationId])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  async function loadMessages() {
    setLoading(true)
    const { data, error } = await supabase
      .from('chat_messages')
      .select('*')
      .eq('application_id', applicationId)
      .order('created_at', { ascending: true })

    if (!error && data) {
      setMessages(data)
    }
    setLoading(false)
  }

  async function sendMessage(e: React.FormEvent) {
    e.preventDefault()
    if (!newMessage.trim()) return

    const { error } = await supabase.from('chat_messages').insert({
      application_id: applicationId,
      sender_id: currentUserId,
      message: newMessage.trim(),
    })

    if (!error) {
      setNewMessage('')
    }
  }

  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  function formatTime(timestamp: string): string {
    const date = new Date(timestamp)
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()

    if (isToday) {
      return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })
    }
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
  }

  function getAvatarInitials(role: string): string {
    switch (role) {
      case 'user':
        return 'Я'
      case 'shelter':
        return 'П'
      case 'admin':
        return 'А'
      default:
        return '?'
    }
  }

  function getAvatarColor(role: string): string {
    switch (role) {
      case 'user':
        return 'bg-primary-600'
      case 'shelter':
        return 'bg-emerald-600'
      case 'admin':
        return 'bg-purple-600'
      default:
        return 'bg-gray-600'
    }
  }

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 px-4 py-3">
        <div className="flex items-center gap-2 text-white">
          <User className="w-5 h-5" />
          <span className="font-semibold">Чат с заявителем</span>
        </div>
      </div>

      {/* Messages */}
      <div className="h-96 overflow-y-auto p-4 bg-gray-50">
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <div className="w-8 h-8 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <User className="w-12 h-12 mb-2 opacity-50" />
            <p className="text-sm">Здесь пока нет сообщений</p>
            <p className="text-xs">Напишите первое сообщение!</p>
          </div>
        ) : (
          <div className="space-y-3">
            {messages.map((msg) => {
              const isOwn = msg.sender_id === currentUserId
              return (
                <div
                  key={msg.id}
                  className={`flex ${isOwn ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`flex gap-2 max-w-[75%] ${isOwn ? 'flex-row-reverse' : ''}`}>
                    {!isOwn && (
                      <div
                        className={`w-8 h-8 rounded-full ${getAvatarColor(
                          msg.sender_role
                        )} flex items-center justify-center flex-shrink-0`}
                      >
                        <span className="text-white text-xs font-bold">
                          {getAvatarInitials(msg.sender_role)}
                        </span>
                      </div>
                    )}
                    <div>
                      {!isOwn && (
                        <div className="text-xs text-gray-500 mb-1 ml-1">
                          {msg.sender_role === 'user' ? 'Заявитель' : 'Приют'}
                        </div>
                      )}
                      <div
                        className={`rounded-2xl px-4 py-2 ${
                          isOwn
                            ? 'bg-primary-600 text-white rounded-br-md'
                            : 'bg-white text-gray-900 rounded-bl-md shadow-sm border border-gray-100'
                        }`}
                      >
                        <p className="text-sm">{msg.message}</p>
                        <p
                          className={`text-xs mt-1 ${
                            isOwn ? 'text-primary-100' : 'text-gray-400'
                          }`}
                        >
                          {formatTime(msg.created_at)}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )
            })}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Input */}
      <form
        onSubmit={sendMessage}
        className="border-t border-gray-100 p-4 bg-white"
      >
        <div className="flex gap-2">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="Напишите сообщение..."
            className="flex-1 px-4 py-2 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent text-sm"
          />
          <button
            type="submit"
            disabled={!newMessage.trim()}
            className="bg-primary-600 hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white p-2.5 rounded-full transition"
          >
            <Send className="w-5 h-5" />
          </button>
        </div>
      </form>
    </div>
  )
}