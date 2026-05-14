<template>
  <div class="about">
	  <headerPage></headerPage>
	  <div class="content">
		  <div class="about-content">
			  <div class="about-desc">
				<div style="font-weight:bold">智学伴</div>
				<div>作者：*********</div>
				<div>学校：*********</div>
				<div>院系：*********</div>
				<div>专业：*********</div>
				<div>邮箱：*********</div>
				<div>Q Q：*********</div>
				<div>微信：*********</div>
				<div>电话：*********</div>
				<div>没有什么烦恼是敲出一行代码解决不了的，不行，就两行</div>
			  </div>
			  <img style="width:59%" src="../../assets/image/about/about.jpeg">
		  </div>
	  </div>
  </div>
</template>

<script>
  import headerPage from "../../components/header/header"
  export default {
	data() {
	  return{

	  }
	},
	components: {
		headerPage,
	},
	methods: {


	},
	created() {

	},
	mounted() {

	}
 }
</script>

<style scoped>
@import url("../../assets/css/about/about.css");
</style>
