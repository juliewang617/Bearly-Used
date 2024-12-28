// INITIALIZES THE SUPABASE CLIENT only once in case it needs to be used
// in multiple places

import { createClient } from "@supabase/supabase-js";

const SUPABASE_ENDPOINT = import.meta.env.VITE_STORAGE_ENDPOINT;
const SUPABASE_KEY = import.meta.env.VITE_KEY;

export const supabase = createClient(SUPABASE_ENDPOINT, SUPABASE_KEY);
