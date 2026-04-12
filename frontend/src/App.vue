<template>
  <div class="night-sky">
    <!-- 6 shooting stars (pure CSS animation, staggered) -->
    <span v-for="n in 6" :key="'ss' + n" :class="['shooting-star', `ss-${n}`]"></span>

    <!-- Twinkle dots (positions generated in data for stable rendering) -->
    <span
      v-for="dot in twinkleDots"
      :key="'td' + dot.id"
      class="twinkle-dot"
      :style="dot.style"
    ></span>

    <h1 class="app-title">Daily Studies</h1>

    <!-- Loading state -->
    <p v-if="!topics.length" class="loading-msg">Loading topics…</p>

    <!-- Pills grid -->
    <div v-else class="pills-grid">
      <div
        v-for="t in topics"
        :key="t.topic"
        class="pill"
        :class="{ 'pill--highlight': t.topic === latestTopic }"
        :style="t.topic === latestTopic ? highlightStyle(t.topic) : {}"
      >
        <!-- Remove button (view mode only, small × in corner) -->
        <button
          v-if="!editMode"
          class="pill__remove"
          @click="confirmRemove(t.topic)"
          title="Remove topic"
        >
          ×
        </button>

        <div class="pill__inner">
          <!-- Topic name + count -->
          <div class="pill__header">
            <span class="pill__name">{{ t.topic }}</span>
            <span class="pill__count">
              {{ editMode ? localState[t.topic].count : t.count }}
            </span>
          </div>

          <!-- Item to study next — read view -->
          <p v-if="!editMode && t.itemToStudyNext" class="pill__next">
            {{ t.itemToStudyNext }}
          </p>

          <!-- Item to study next — edit view -->
          <input
            v-if="editMode"
            v-model="localState[t.topic].itemToStudyNext"
            class="pill__next-input"
            placeholder="Next item to study…"
          />

          <!-- +/− controls (edit mode only) -->
          <div v-if="editMode" class="pill__controls">
            <button class="ctrl-btn" @click="adjust(t.topic, -1)">−</button>
            <button class="ctrl-btn" @click="adjust(t.topic, +1)">+</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Global action bar -->
    <div class="action-bar">
      <template v-if="!editMode">
        <button class="btn btn--primary" @click="startEdit">Update</button>
        <button class="btn btn--ghost" @click="showAdd = !showAdd">
          {{ showAdd ? '− Cancel Add' : '+ Add Topic' }}
        </button>
      </template>
      <template v-else>
        <button class="btn btn--success" :disabled="saving" @click="saveAll">
          {{ saving ? 'Saving…' : 'Done' }}
        </button>
        <button class="btn btn--ghost" @click="cancelEdit">Cancel</button>
      </template>
    </div>

    <!-- Collapsible add-topic form -->
    <transition name="slide-fade">
      <form v-if="showAdd && !editMode" class="add-form" @submit.prevent="addTopic">
        <input
          v-model="newTopicName"
          class="add-form__input"
          placeholder="New topic name"
          autofocus
        />
        <button type="submit" class="btn btn--primary">Add</button>
      </form>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App',

  data() {
    return {
      topics: [],
      editMode: false,
      localState: {}, // { [topicName]: { count, itemToStudyNext } }
      saving: false,
      showAdd: false,
      newTopicName: '',

      // Map each topic to a highlight image in frontend/public/images/.
      // Add your own image files with matching names.
      topicImages: {
        Technical: '/images/technical.jpg',
        'Non-Technical': '/images/non-technical.jpg',
        Investments: '/images/investments.jpg',
        Security: '/images/security.jpg',
        AI: '/images/ai.jpg',
        Stocks: '/images/stocks.jpg',
      },

      // 70 twinkle dots. Positions computed once in data so the list is stable.
      twinkleDots: Array.from({ length: 70 }, (_, i) => ({
        id: i,
        style: {
          left: `${((i * 137.508) % 100).toFixed(2)}%`,
          top: `${((i * 73.917) % 100).toFixed(2)}%`,
          width: `${1 + (i % 3)}px`,
          height: `${1 + (i % 3)}px`,
          animationDelay: `${((i * 0.41) % 5).toFixed(2)}s`,
          animationDuration: `${(2 + (i % 4)).toFixed(1)}s`,
        },
      })),
    }
  },

  computed: {
    latestTopic() {
      if (!this.topics.length) return null
      return this.topics.reduce((a, b) => {
        if (a.count === b.count) {
          return new Date(b.lastStudiedDate).getTime() > new Date(a.lastStudiedDate).getTime()
            ? a
            : b
        }
        return b.count > a.count ? b : a
      }).topic
    },
  },

  async mounted() {
    await this.fetchTopics()
  },

  methods: {
    async fetchTopics() {
      const res = await fetch('/api/topics')
      this.topics = await res.json()
    },

    startEdit() {
      this.localState = Object.fromEntries(
        this.topics.map((t) => [
          t.topic,
          {
            count: t.count,
            itemToStudyNext: t.itemToStudyNext ?? '',
            initialCount: t.count,
            itemToStudyNextInitial: t.itemToStudyNext ?? '',
          },
        ]),
      )
      this.editMode = true
    },

    cancelEdit() {
      this.editMode = false
      this.localState = {}
    },

    adjust(topicName, delta) {
      const next = this.localState[topicName].count + delta
      this.localState[topicName].count = next
    },

    async saveAll() {
      this.saving = true
      try {
        const responses = await Promise.all(
          this.topics.map((t) => {
            const s = this.localState[t.topic]
            if (!this.isTopicChanged(t.topic)) return Promise.resolve()
            return fetch(`/api/topics/${encodeURIComponent(t.topic)}`, {
              method: 'PUT',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                count: s.count,
                itemToStudyNext: s.itemToStudyNext || null,
              }),
            })
          }),
        )
        if (responses.some((r) => r && !r.ok)) {
          alert('One or more topics failed to save. Please try again.')
          return
        }
        this.editMode = false
        await this.fetchTopics()
      } finally {
        this.saving = false
      }
    },

    async addTopic() {
      const name = this.newTopicName.trim()
      if (!name) return
      const res = await fetch('/api/topics', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ topic: name }),
      })
      if (!res.ok) {
        alert(`Could not add topic "${name}". It may already exist.`)
        return
      }
      this.newTopicName = ''
      this.showAdd = false
      await this.fetchTopics()
    },

    async confirmRemove(topicName) {
      if (!window.confirm(`Remove "${topicName}"?`)) return
      await fetch(`/api/topics/${encodeURIComponent(topicName)}`, { method: 'DELETE' })
      await this.fetchTopics()
    },

    // Returns inline :style for the highlight pill.
    // A dark overlay is baked into the gradient so text is always readable.
    highlightStyle(topicName) {
      const img = this.topicImages[topicName]
      if (!img) return {}
      return {
        backgroundImage: `linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), url("${img}")`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        border: '2px solid #f0c040',
        boxShadow: '0 0 32px rgba(240, 192, 64, 0.55)',
      }
    },
    isTopicChanged(topicName) {
      const s = this.localState[topicName]
      return s.count !== s.initialCount || s.itemToStudyNext !== s.itemToStudyNextInitial
    },
  },
}
</script>

<style lang="scss">
/* ─── Reset ─────────────────────────────────────────────────────────────── */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family:
    'Segoe UI',
    system-ui,
    -apple-system,
    sans-serif;
  background: #050510;
  -webkit-font-smoothing: antialiased;
}

/* ─── Night sky canvas ───────────────────────────────────────────────────── */
.night-sky {
  min-height: 100vh;
  background: linear-gradient(175deg, #080826 0%, #06061a 45%, #020210 80%, #000008 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3.5rem 2rem;
  gap: 2.5rem;
}

/* ─── Twinkle star dots ──────────────────────────────────────────────────── */
@keyframes twinkle {
  0%,
  100% {
    opacity: 0.12;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.4);
  }
}

.twinkle-dot {
  position: absolute;
  border-radius: 50%;
  background: #ffffff;
  animation: twinkle ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}

/* ─── Shooting stars ─────────────────────────────────────────────────────── */
@keyframes shooting-travel {
  0% {
    opacity: 0;
    transform: rotate(30deg) translateX(-60px);
  }
  6% {
    opacity: 1;
    transform: rotate(30deg) translateX(0px);
  }
  82% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: rotate(30deg) translateX(700px);
  }
}

.shooting-star {
  position: absolute;
  width: 130px;
  height: 1.5px;
  border-radius: 1px;
  /* bright core that fades on both ends */
  background: linear-gradient(
    to right,
    transparent 0%,
    rgba(255, 255, 255, 0.15) 15%,
    rgba(255, 255, 255, 0.95) 50%,
    rgba(255, 255, 255, 0.15) 85%,
    transparent 100%
  );
  transform-origin: center center;
  animation: shooting-travel linear infinite;
  opacity: 0;
  pointer-events: none;
  z-index: 2;

  &.ss-1 {
    top: 6%;
    left: 12%;
    animation-duration: 4s;
    animation-delay: 0s;
  }
  &.ss-2 {
    top: 3%;
    left: 58%;
    animation-duration: 3.5s;
    animation-delay: 2.1s;
  }
  &.ss-3 {
    top: 18%;
    left: 8%;
    animation-duration: 4.8s;
    animation-delay: 4.4s;
  }
  &.ss-4 {
    top: 1%;
    left: 38%;
    animation-duration: 3.8s;
    animation-delay: 1.2s;
  }
  &.ss-5 {
    top: 10%;
    left: 75%;
    animation-duration: 5.2s;
    animation-delay: 3.6s;
  }
  &.ss-6 {
    top: 22%;
    left: 45%;
    animation-duration: 4.2s;
    animation-delay: 6s;
  }
}

/* ─── Title ──────────────────────────────────────────────────────────────── */
.app-title {
  font-size: 2.2rem;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.88);
  position: relative;
  z-index: 10;
  text-shadow: 0 0 24px rgba(120, 160, 255, 0.4);
}

.loading-msg {
  color: rgba(255, 255, 255, 0.45);
  font-size: 1rem;
  z-index: 10;
}

/* ─── Pills grid ─────────────────────────────────────────────────────────── */
.pills-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  justify-content: center;
  max-width: 1000px;
  width: 100%;
  position: relative;
  z-index: 10;
}

/* ─── Single pill ────────────────────────────────────────────────────────── */
.pill {
  position: relative;
  /* Glass-morphism: night sky and shooting stars show through */
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.11);
  border-radius: 20px;
  padding: 1.4rem 1.6rem;
  min-width: 175px;
  color: #fff;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.55);
  }

  /* Highest-count pill: background image + golden glow set via :style binding */
  &--highlight {
    border-radius: 20px; /* kept so border-radius isn't lost when :style adds border */
  }

  /* Small × in the top-right corner */
  &__remove {
    position: absolute;
    top: 10px;
    right: 12px;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.3);
    font-size: 1rem;
    line-height: 1;
    cursor: pointer;
    padding: 2px 4px;
    border-radius: 4px;
    transition:
      color 0.15s,
      background 0.15s;

    &:hover {
      color: #ff6b6b;
      background: rgba(255, 100, 100, 0.12);
    }
  }

  &__inner {
    display: flex;
    flex-direction: column;
    gap: 0.55rem;
  }

  &__header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 1rem;
  }

  &__name {
    font-size: 0.82rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: rgba(255, 255, 255, 0.82);
    white-space: nowrap;
  }

  &__count {
    font-size: 2.4rem;
    font-weight: 700;
    line-height: 1;
    color: #ffffff;
  }

  &__next {
    font-size: 0.76rem;
    color: rgba(255, 255, 255, 0.5);
    font-style: italic;
    line-height: 1.35;
  }

  &__next-input {
    width: 100%;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 8px;
    padding: 0.38rem 0.65rem;
    font-size: 0.8rem;
    color: #fff;
    outline: none;
    transition: border-color 0.15s;

    &::placeholder {
      color: rgba(255, 255, 255, 0.3);
    }
    &:focus {
      border-color: rgba(255, 255, 255, 0.45);
    }
  }

  &__controls {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.15rem;
  }
}

/* ─── +/− count buttons ──────────────────────────────────────────────────── */
.ctrl-btn {
  flex: 1;
  padding: 0.2rem 0;
  font-size: 1.3rem;
  line-height: 1;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
  &:active {
    background: rgba(255, 255, 255, 0.28);
  }
}

/* ─── Action bar ─────────────────────────────────────────────────────────── */
.action-bar {
  display: flex;
  gap: 0.9rem;
  position: relative;
  z-index: 10;
}

/* ─── Buttons ────────────────────────────────────────────────────────────── */
.btn {
  padding: 0.58rem 1.7rem;
  border: none;
  border-radius: 30px;
  font-size: 0.88rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition:
    transform 0.12s,
    opacity 0.12s,
    background 0.15s;

  &:active {
    transform: scale(0.96);
  }
  &:disabled {
    opacity: 0.55;
    cursor: default;
  }

  &--primary {
    background: #4a8fe2;
    color: #fff;
    &:hover:not(:disabled) {
      background: #5fa3f5;
    }
  }

  &--success {
    background: #3ab06a;
    color: #fff;
    &:hover:not(:disabled) {
      background: #4dc97c;
    }
  }

  &--ghost {
    background: rgba(255, 255, 255, 0.07);
    color: rgba(255, 255, 255, 0.78);
    border: 1px solid rgba(255, 255, 255, 0.16);
    &:hover {
      background: rgba(255, 255, 255, 0.14);
    }
  }
}

/* ─── Add-topic form ─────────────────────────────────────────────────────── */
.add-form {
  display: flex;
  gap: 0.75rem;
  position: relative;
  z-index: 10;

  &__input {
    background: rgba(255, 255, 255, 0.07);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 10px;
    padding: 0.55rem 1rem;
    font-size: 0.9rem;
    color: #fff;
    outline: none;
    min-width: 200px;
    transition: border-color 0.15s;

    &::placeholder {
      color: rgba(255, 255, 255, 0.32);
    }
    &:focus {
      border-color: rgba(255, 255, 255, 0.48);
    }
  }
}

/* ─── Slide-fade transition for add form ────────────────────────────────── */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
