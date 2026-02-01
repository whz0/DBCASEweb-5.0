<template>
  <header>
    <h1>Bienvenido</h1>
  </header>
  <nav class="sticky-top navbar navbar-expand-lg navbar-dark">

    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarBar" aria-controls="navbarBar" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>

    <div id="navbarBar" class="collapse navbar-collapse">
      <ul class="navbar-nav me-auto">
        <li class="nav-item">
          <button class="btn-outline-light">
            Dibujar
          </button>
          <button class="btn-outline-light">
            Archivo
          </button>
          <button class="btn-outline-light">
            Opciones
          </button>
          <button class="btn-outline-light">
            Diseño
          </button>
          <button class="btn-outline-light">
            Ayuda
          </button>
        </li>
      </ul>
    </div>

    <a class="navbar-brand" href="#">
      <img id="logo" class="d-inline-block align-middle" src="./assets/logo.png" alt="Logo de DBCase" style="width: 30px"> DBCase
    </a>

    <button type="button" class="rounded-5 dropdown-toggle" data-bs-toggle="dropdown">
      <i class="bi">AA</i>
    </button>
    <div class="dropdown-menu dropdown-menu-end">
      <a href="/perfil" class="dropdown-item">Hola</a>
      <form action="/logout" method="POST">
        <button type="submit" class="dropdown-item text-daner">Cerrar sesión</button>
      </form>
    </div>
  </nav>
  <main class="container-fluid">
    <div class="row">
      <PanelERScheme/>
      <div class="col-md-6">
        <div class="container">
          <PanelLogicalScheme/>
          <PanelDBScheme/>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import PanelERScheme from './components/PanelERScheme.vue'
import PanelLogicalScheme from "@/components/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/PanelDBScheme.vue";

export default {
  name: 'App',
  components: {
    PanelLogicalScheme,
    PanelERScheme,
    PanelDBScheme
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  margin-top: 60px;
  background-color: hsl(210, 10%, 23%);
  height: 100vh;
}

h1, h2 {
  color: white;
}

button {
  color: hsl(128, 50%, 50%);
}
</style>
