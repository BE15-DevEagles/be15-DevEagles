<template>
  <ul class="pagination">
    <li class="pagination-item">
      <a
        class="pagination-link pagination-prev"
        :class="{ disabled: currentPage === 1 }"
        href="#"
        @click.prevent="onPrevPage"
      >
        &laquo;
      </a>
    </li>

    <li v-for="page in displayedPages" :key="page" class="pagination-item">
      <template v-if="page === '...'">
        <span class="pagination-ellipsis">{{ page }}</span>
      </template>
      <template v-else>
        <a
          class="pagination-link"
          :class="{ active: page === currentPage }"
          href="#"
          @click.prevent="onPageChange(page)"
        >
          {{ page }}
        </a>
      </template>
    </li>

    <li class="pagination-item">
      <a
        class="pagination-link pagination-next"
        :class="{ disabled: currentPage === totalPages }"
        href="#"
        @click.prevent="onNextPage"
      >
        &raquo;
      </a>
    </li>
  </ul>
</template>

<script>
  export default {
    name: 'BasePagination',
    props: {
      currentPage: {
        type: Number,
        required: true,
      },
      totalPages: {
        type: Number,
        required: true,
      },
    },
    emits: ['update:currentPage'],
    computed: {
      displayedPages() {
        const pages = [];
        const totalPages = this.totalPages;
        const currentPage = this.currentPage;

        if (totalPages <= 7) {
          for (let i = 1; i <= totalPages; i++) {
            pages.push(i);
          }
          return pages;
        }

        if (currentPage <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i);
          }
          pages.push('...');
          pages.push(totalPages);
        } else if (currentPage >= totalPages - 3) {
          pages.push(1);
          pages.push('...');
          for (let i = totalPages - 4; i <= totalPages; i++) {
            pages.push(i);
          }
        } else {
          pages.push(1);
          pages.push('...');
          pages.push(currentPage - 1);
          pages.push(currentPage);
          pages.push(currentPage + 1);
          pages.push('...');
          pages.push(totalPages);
        }
        return pages;
      },
    },
    methods: {
      onPageChange(page) {
        this.$emit('update:currentPage', page);
      },
      onPrevPage() {
        if (this.currentPage > 1) {
          this.$emit('update:currentPage', this.currentPage - 1);
        }
      },
      onNextPage() {
        if (this.currentPage < this.totalPages) {
          this.$emit('update:currentPage', this.currentPage + 1);
        }
      },
    },
  };
</script>
