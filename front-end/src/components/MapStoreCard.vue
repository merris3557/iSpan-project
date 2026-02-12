<script setup>
const props = defineProps({
    store: {
        type: Object,
        required: true,
        default: () => ({
            name: '店家名稱N/A',
            image: 'https://placehold.co/150', // fast placeholder
            hours: '營業時間N/A',
            description: '暫無簡介'
        })
    }
})
</script>

<template>
    <div class="card mb-3 card-gdg rounded-4 overflow-hidden">
        <div class="row g-0">
            <div class="col-md-4">
                <img :src="props.store.image || 'https://placehold.co/150'" 
                     class="img-fluid rounded-start h-100 object-fit-cover" 
                     :alt="props.store.name"
                     style="min-height: 150px;">
            </div>
            <div class="col-md-8">
                <div class="card-body">
                    <h5 class="card-title text-gdg fw-bold">{{ props.store.name }}</h5>
                    <p class="card-text mb-1">
                        <small class="text-body-secondary">
                            <i class="bi bi-clock me-1"></i>{{ props.store.hours }}
                        </small>
                    </p>
                    <p class="card-text description-text">{{ props.store.description }}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.object-fit-cover {
    object-fit: cover;
    width: 100%;
}
.description-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    font-size: 0.9rem;
}
</style>