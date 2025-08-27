<template>
  <div class="confirm-modal-overlay" @click.self="$emit('cancel')">
    <div class="confirm-modal">
      <div class="confirm-header">
        <h3>{{ title }}</h3>
        <button class="close-btn" @click="$emit('cancel')">&times;</button>
      </div>
      
      <div class="confirm-body">
        <p>{{ message }}</p>
      </div>
      
      <div class="confirm-actions">
        <button class="btn-cancel" @click="$emit('cancel')">
          취소
        </button>
        <button class="btn-confirm" @click="$emit('confirm')">
          확인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '확인'
  },
  message: {
    type: String,
    required: true
  }
});

defineEmits(['confirm', 'cancel']);
</script>

<style scoped>
.confirm-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 0 16px;
}

.confirm-modal {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.confirm-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #111827;
}

.confirm-body {
  padding: 24px 20px;
  color: #4b5563;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  background-color: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-confirm {
  padding: 8px 20px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-cancel {
  background: white;
  border: 1px solid #d1d5db;
  color: #4b5563;
}

.btn-cancel:hover {
  background: #f3f4f6;
}

.btn-confirm {
  background: #ef4444;
  border: 1px solid #ef4444;
  color: white;
}

.btn-confirm:hover {
  background: #dc2626;
  border-color: #dc2626;
}

/* 반응형 스타일 */
@media (max-width: 480px) {
  .confirm-actions {
    flex-direction: column;
  }
  
  .btn-cancel,
  .btn-confirm {
    width: 100%;
  }
}
</style>
