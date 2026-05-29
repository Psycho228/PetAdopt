export type PetType = 'dog' | 'cat' | 'bird' | 'other'
export type PetGender = 'male' | 'female'
export type PetSize = 'small' | 'medium' | 'large'
export type AppStatus = 'pending' | 'processing' | 'approved' | 'rejected'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH'

export interface User {
  id: string
  email: string
  name: string
  phone?: string
  city?: string
  role?: string
  avatar_url?: string
  created_at?: string
}

export interface Pet {
  id: string
  shelter_id: string
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
  photo_url: string
  additional_photos: string[] | null
  is_neutered: boolean
  has_vaccination: boolean
  is_active: boolean
  created_at?: string
  updated_at?: string
}

export interface Application {
  id: string
  user_id: string
  user_name: string
  user_email: string
  pet_id: string
  pet_name: string
  message: string
  contact_time: string
  contact_days: string
  status: AppStatus
  created_at?: string
  updated_at?: string
}

export interface QuestionnaireAnswer {
  id: string
  user_id: string
  q1_full_name?: string
  q1_age?: number
  q1_city?: string
  q1_occupation?: string
  q1_contact_method?: string
  q2_housing_type?: string
  q2_pets_allowed?: boolean
  q2_living_with?: string[]
  q2_family_consent?: boolean
  q2_has_children?: boolean
  q2_children_ages?: string
  q2_has_other_pets?: boolean
  q2_other_pets_types?: string[]
  q2_hours_alone?: number
  q2_caregiver?: string
  q3_had_pets_before?: boolean
  q3_what_happened?: string
  q3_dog_experience?: boolean
  q3_cat_experience?: boolean
  q3_special_needs_experience?: boolean
  q3_why_now?: string
  q4_furniture_damage_plan?: string
  q4_noise_plan?: string
  q4_shy_pet_plan?: string
  q4_long_adaptation_plan?: string
  q4_life_changes_plan?: string
  q4_obstacles_next_year?: string
  q4_understand_requirements?: boolean
  q4_understand_time?: boolean
  q4_understand_attention?: boolean
  q4_understand_training?: boolean
  q4_understand_vet_care?: boolean
  q4_ready_expenses?: boolean
  q4_ready_food?: boolean
  q4_ready_vet?: boolean
  q4_ready_medication?: boolean
  q4_ready_vaccinations?: boolean
  q4_ready_grooming?: boolean
  q4_ready_education?: boolean
  q5_safety_measures?: string[]
  q5_ready_neuter?: boolean
  q5_ready_recommendations?: boolean
  q5_ready_tracker?: boolean
  q5_ready_keep_contact?: boolean
  q6_responsible_owner_meaning?: string
  q6_life_with_pet_vision?: string
  q6_why_good_owner?: string
  q7_desired_pets?: string[]
  created_at?: string
  updated_at?: string
}

export interface RiskAssessment {
  id: string
  user_id: string
  questionnaire_answer_id: string
  overallRisk: string
  riskScore: number
  recommendation: string
  detailedAnalysis: string
  riskFactorsJson: string
  positiveFactorsJson: string
  recommendationsJson: string
  created_at: string
  gigachat_request_id: string
}

export interface Shelter {
  id: string
  user_id: string
  name: string
  description?: string
  address?: string
  phone?: string
  website?: string
  is_verified: boolean
  created_at?: string
}

// Derived types for UI
export interface RiskFactor {
  title: string
  description: string
  severity: 'high' | 'medium' | 'low'
}

export interface DashboardStats {
  totalPets: number
  activePets: number
  totalApplications: number
  pendingApplications: number
  approvedApplications: number
  rejectedApplications: number
  processingApplications: number
}
