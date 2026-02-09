<template>
  <svg ref="barcodeEl" class="barcode-svg" />
</template>

<script lang="ts">
import { defineComponent, nextTick, onMounted, ref, watch } from "vue";
import JsBarcode from "jsbarcode";

export default defineComponent({
  name: "BarcodeSvg",
  props: {
    value: {
      type: String,
      required: true,
    },
    height: {
      type: Number,
      default: 40,
    },
    width: {
      type: Number,
      default: 2,
    },
    displayValue: {
      type: Boolean,
      default: false,
    },
  },
  setup(props) {
    const barcodeEl = ref<SVGSVGElement | null>(null);

    const renderBarcode = () => {
      if (!barcodeEl.value) return;
      if (!props.value) {
        barcodeEl.value.innerHTML = "";
        return;
      }
      try {
        JsBarcode(barcodeEl.value, props.value, {
          format: "CODE128",
          displayValue: props.displayValue,
          height: props.height,
          width: props.width,
          margin: 0,
        });
      } catch (error) {
        barcodeEl.value.innerHTML = "";
        console.error("Не удалось построить штрихкод:", error);
      }
    };

    onMounted(() => {
      void nextTick(renderBarcode);
    });

    watch(
      () => [props.value, props.height, props.width, props.displayValue],
      () => {
        void nextTick(renderBarcode);
      }
    );

    return {
      barcodeEl,
    };
  },
});
</script>

<style scoped>
.barcode-svg {
  width: 100%;
  max-width: 220px;
  height: auto;
}
</style>
