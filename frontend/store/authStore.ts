import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { fetchApi } from '@/lib/api';

interface User {
  userId: number;
  username: string;
  firstName?: string;
  lastName?: string;
  selectedItemId?: number | null;
}

interface AuthState {
  token: string | null;
  user: User | null;
  hydrated: boolean;
  setAuth: (token: string, user: User) => void;
  logout: () => Promise<void>;
  hydrate: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      hydrated: false,
      setAuth: (token, user) => set({ token, user }),
      logout: async () => {
        try {
          if (get().token) {
            await fetchApi('/auth/logout', { method: 'POST' });
          }
        } catch (e) {
          // ignore error if token already expired
        }
        set({ token: null, user: null });
      },
      hydrate: async () => {
        set({ hydrated: true });
        const { token, user } = get();
        if (token) {
          try {
            const me = await fetchApi('/auth/me');
            // /auth/me returns { userId, username, selectedItemId }
            // Merge with existing user data to preserve firstName/lastName from login
            set({
              user: {
                ...user,
                userId: me.userId,
                username: me.username,
                selectedItemId: me.selectedItemId,
              } as User,
            });
          } catch (e) {
            set({ token: null, user: null });
          }
        }
      },
    }),
    {
      name: 'primebid-auth',
      onRehydrateStorage: () => (state) => {
        state?.hydrate();
      },
    }
  )
);
