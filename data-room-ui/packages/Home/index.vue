<template>
  <div class="centered">
    <p>Hello</p>
  </div>
</template>
<script>
import { BigScreenManagement } from '@gcpaas/data-room-ui'
export default {
  components: {
    BigScreenManagement
  }
}
</script>
<style>
.centered {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color:black;
  font-size: 300px;
}
</style>
