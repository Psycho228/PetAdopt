package com.example.petadopt.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.createSupabaseClient

object SupabaseConfig {
    const val SUPABASE_URL = "https://zbnfyovmeipmgpyfimbx.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpibmZ5b3ZtZWlwbWdweWZpbWJ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkxMjI3NjgsImV4cCI6MjA5NDY5ODc2OH0.aL3YaYBL6poWIzV6OAY-y44JQGubfuhCjQ2RcjEni-o"

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    val auth: Auth get() = supabaseClient.pluginManager.getPlugin(Auth)
    val postgrest: Postgrest get() = supabaseClient.pluginManager.getPlugin(Postgrest)
    val storage: Storage get() = supabaseClient.pluginManager.getPlugin(Storage)
}
