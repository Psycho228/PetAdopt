package com.example.petadopt.util

import com.example.petadopt.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.createSupabaseClient

object SupabaseConfig {
    val SUPABASE_URL: String
        get() = BuildConfig.SUPABASE_URL

    val SUPABASE_ANON_KEY: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val supabaseClient: SupabaseClient by lazy {
        require(SUPABASE_URL.isNotBlank()) { "SUPABASE_URL is missing. Add it to the root .env file." }
        require(SUPABASE_ANON_KEY.isNotBlank()) { "SUPABASE_ANON_KEY is missing. Add it to the root .env file." }

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
