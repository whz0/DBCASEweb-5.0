<template>
  <header>
    <nav class="fixed-top navbar navbar-expand-lg">

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

      <a class="navbar-brand" href="/">
        <img id="logo" class="d-inline-block align-middle" src="./assets/logo.png" alt="Logo de DBCase" style="width: 30px"> DBCase
      </a>

      <button type="button" class="dropdown-toggle" data-bs-toggle="dropdown">
        <i class="bi bi-person-circle"></i>
      </button>
      <div class="dropdown-menu dropdown-menu-end">
        <RouterLink class="dropdown-item" to="/profile">Perfil</RouterLink>
        <form id="logoutForm">
          <button type="submit" class="dropdown-item text-daner">Cerrar sesión</button>
        </form>
      </div>
    </nav>
  </header>
  <main class="container-fluid">
    <RouterView />
  </main>
</template>

<script>
export default {
  name: 'App'
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

header {
  background-color: hsl(210, 10%, 23%);
}

nav {
  background-color: hsl(210, 10%, 23%);
}

h1, h2 {
  color: white;
}

button {
  background-color: transparent;
  color: hsl(128, 50%, 50%);
  border-style: none;
}

i {
  width: 5rem;
}
</style>
