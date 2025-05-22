<template>
  <footer
    class="bg-[var(--color-gray-800)] h-12 w-full flex items-center justify-center px-2 sm:px-4"
  >
    <div class="flex items-center justify-between w-full max-w-7xl text-xs">
      <div class="flex items-center min-w-0">
        <div class="flex-shrink min-w-0">
          <p
            class="text-[var(--color-gray-300)] truncate"
            title="DevEagles' Goody Project | Hanwha Beyond Software Bootcamp, 15th Cohort"
          >
            <span class="font-semibold">Goody Project</span>
            <span class="text-[var(--color-gray-500)]"> | Team DevEagles, 2025</span>
          </p>
        </div>
        <p class="text-[var(--color-gray-500)] hidden md:block pl-1 whitespace-nowrap ml-2">
          Hanwha Beyond Software Bootcamp, 15th Cohort
        </p>
      </div>

      <div class="flex items-center flex-shrink-0 space-x-2 sm:space-x-3">
        <span class="text-[var(--color-gray-400)] whitespace-nowrap">v1.0.0</span>

        <a
          href="https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody"
          target="_blank"
          rel="noopener noreferrer"
          class="group text-[var(--color-gray-300)] hover:text-white transition-colors duration-200 flex items-center"
          title="GitHub Repository"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-4 h-4 group-hover:stroke-sky-400 transition-colors duration-200"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M17.25 6.75 22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3-4.5 16.5"
            />
          </svg>
          <span class="sr-only sm:not-sr-only sm:ml-1 whitespace-nowrap">GitHub</span>
        </a>

        <a
          href="https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/wiki"
          target="_blank"
          rel="noopener noreferrer"
          class="group text-[var(--color-gray-300)] hover:text-white font-medium transition-colors duration-200 flex items-center"
          title="Wiki"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-4 h-4 group-hover:stroke-sky-400 transition-colors duration-200"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6-2.292m0 0V21M12 12.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5Z"
            />
          </svg>
          <span class="sr-only sm:not-sr-only sm:ml-1 whitespace-nowrap">Wiki</span>
        </a>

        <a
          href="#"
          class="group text-[var(--color-gray-300)] hover:text-white font-medium transition-colors duration-200 flex items-center"
          title="Terms of Service"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-4 h-4 group-hover:stroke-sky-400 transition-colors duration-200"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"
            />
          </svg>
          <span class="sr-only sm:not-sr-only sm:ml-1 whitespace-nowrap">Terms</span>
        </a>

        <a
          href="#"
          class="group text-[var(--color-gray-300)] hover:text-white font-medium transition-colors duration-200 flex items-center"
          title="Privacy Policy"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-4 h-4 group-hover:stroke-sky-400 transition-colors duration-200"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M9 12.75 11.25 15 15 9.75m-3-7.036A11.959 11.959 0 0 1 3.598 6 11.99 11.99 0 0 0 3 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285Z"
            />
          </svg>
          <span class="sr-only sm:not-sr-only sm:ml-1 whitespace-nowrap">Privacy</span>
        </a>
      </div>
    </div>
  </footer>
</template>

<script setup>
  // Footer Component
</script>
