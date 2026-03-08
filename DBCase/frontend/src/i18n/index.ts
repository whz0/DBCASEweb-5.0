import { createI18n } from 'vue-i18n';

const messages = {
  en: {
    toolbar: {
      draw: 'Draw',
      openFile: 'Open file',
      layout: 'Layout',
      settings: 'Settings',
      help: 'Help',
      aboutUs: 'About Us',
      saveSchema: 'Save schema',
      openSchema: 'Open schema',
      greeting: 'Hello {name}',
      drawMenuItems: {
        entity: 'Entity',
        attribute: 'Attribute', // Added
        relationship: 'Relationship',
        simple: 'Simple',
        isA: 'IsA',
        domain: 'Domain',
        composite: 'Composite'
      }
    },
    layout: {
      horizontal: 'Horizontal',
      vertical: 'Vertical',
      chooseLayout: 'Choose Layout'
    },
    settings: {
      title: 'Settings',
      language: 'Language',
      theme: 'Theme',
      light: 'Light',
      dark: 'Dark',
      system: 'System',
      english: 'English',
      spanish: 'Spanish'
    },
    panels: {
      conceptual: 'Conceptual Schema',
      logical: 'Logical Schema',
      physical: 'Physical Schema',
      close: 'Close',
      insertRelationship: 'Insert new relationship',
      insertIsARelationship: 'Insert new IsA relationship',
      createDomain: 'Create domain'
    },
    entity: {
      addEntity: 'Add Entity',
      entityName: 'Entity Name',
      enterEntityName: 'Enter entity name',
      isWeakEntity: 'Is Weak Entity?',
      weakRelationshipName: 'Weak Relationship Name',
      enterRelationName: 'Enter relation name',
      strongEntity: 'Strong Entity',
      selectStrongEntity: 'Select a Strong Entity',
      strongEntityA: 'Strong Entity A',
      strongEntityB: 'Strong Entity B'
    },
    attribute: {
      addAttribute: 'Add Attribute',
      attributeName: 'Attribute Name',
      enterAttributeName: 'Enter attribute name',
      parentEntity: 'Parent Entity',
      selectParentEntity: 'Select Parent Entity'
    },
    relationship: {
      addRelationship: 'Add Relationship',
      relationshipName: 'Relationship Name',
      enterRelationshipName: 'Enter relationship name',
      cardinality: 'Cardinality',
      selectEntity: 'Select entity',
      entity: 'Entity'
    },
    schema: {
      save: 'Save (.dbw)',
      schemaName: 'Schema Name',
      enterSchemaName: 'Enter schema name',
      generate: 'Generate Schemes',
      generateER: 'Generate ER Scheme',
      generateLogical: 'Generate Logical Scheme',
      generatePhysical: 'Generate Physical Scheme',
      selectFile: 'Select File (.dbw)',
      open: 'Open',
      selectedFile: 'Selected File'
    },
    errors: {
      404: '404',
      500: '500',
      pageNotFound: 'Page Not Found',
      pageNotFoundMessage: "The page you're looking for doesn't exist or has been moved.",
      internalServerError: 'Internal Server Error',
      internalServerErrorMessage: 'Something went wrong on our end. Please try again later.',
      reloadPage: 'Reload Page'
    },
    common: {
      chooseFile: 'Choose a file',
      cancel: 'Cancel',
      save: 'Save',
      add: 'Add', // Added
      goHome: 'Go Home',
      goBack: 'Go Back',
      profile: 'Profile',
      logout: 'Logout'
    },
    aboutUs: {
      title: 'About Us',
      header: 'About Us',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'
    },
    help: {
      title: 'Help',
      header: 'Help',
      content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'
    },
    profile: {
      clickMe: 'You clicked me {count} times'
    },
    login: {
      title: 'Sign In',
      subtitle: 'Please sign in to continue',
      username: 'Username',
      password: 'Password',
      login: 'Login',
      or: 'OR',
      google: 'Sign in with Google',
      github: 'Sign in with GitHub',
      noAccount: "Don't have an account?",
      createAccount: 'Create one now'
    }
  },
  es: {
    toolbar: {
      draw: 'Dibujar',
      openFile: 'Abrir archivo',
      layout: 'Diseño',
      settings: 'Configuración',
      help: 'Ayuda',
      aboutUs: 'Acerca de',
      saveSchema: 'Guardar esquema',
      openSchema: 'Abrir esquema',
      greeting: 'Hola {name}',
      drawMenuItems: {
        entity: 'Entidad',
        attribute: 'Atributo', // Added
        relationship: 'Relación',
        simple: 'Simple',
        isA: 'IsA',
        domain: 'Dominio',
        composite: 'Compuesto'
      }
    },
    layout: {
      horizontal: 'Horizontal',
      vertical: 'Vertical',
      chooseLayout: 'Elegir diseño'
    },
    settings: {
      title: 'Configuración',
      language: 'Idioma',
      theme: 'Tema',
      light: 'Claro',
      dark: 'Oscuro',
      system: 'Sistema',
      english: 'Inglés',
      spanish: 'Español'
    },
    panels: {
      conceptual: 'Esquema Conceptual',
      logical: 'Esquema Lógico',
      physical: 'Esquema Físico',
      close: 'Cerrar',
      insertRelationship: 'Insertar una nueva relación',
      insertIsARelationship: 'Insertar una nueva relación IsA',
      createDomain: 'Crear dominio'
    },
    entity: {
      addEntity: 'Añadir Entidad',
      entityName: 'Nombre de Entidad',
      enterEntityName: 'Introduce el nombre de la entidad',
      isWeakEntity: '¿Es Entidad Débil?',
      weakRelationshipName: 'Nombre de Relación Débil',
      enterRelationName: 'Introduce el nombre de la relación',
      strongEntity: 'Entidad Fuerte',
      selectStrongEntity: 'Selecciona una Entidad Fuerte',
      strongEntityA: 'Entidad Fuerte A',
      strongEntityB: 'Entidad Fuerte B'
    },
    attribute: {
      addAttribute: 'Añadir Atributo',
      attributeName: 'Nombre de Atributo',
      enterAttributeName: 'Introduce el nombre del atributo',
      parentEntity: 'Entidad Padre',
      selectParentEntity: 'Selecciona la Entidad Padre'
    },
    relationship: {
      addRelationship: 'Añadir Relación',
      relationshipName: 'Nombre de la Relación',
      enterRelationshipName: 'Introduce el nombre de la relación',
      cardinality: 'Cardinalidad',
      selectEntity: 'Seleccionar entidad',
      entity: 'Entidad'
    },
    schema: {
      save: 'Guardar (.dbw)',
      schemaName: 'Nombre del Esquema',
      enterSchemaName: 'Introduce el nombre del esquema',
      generate: 'Generar Esquemas',
      generateER: 'Generar Esquema ER',
      generateLogical: 'Generar Esquema Lógico',
      generatePhysical: 'Generar Esquema Físico',
      selectFile: 'Seleccionar Archivo (.dbw)',
      open: 'Abrir',
      selectedFile: 'Archivo Seleccionado'
    },
    errors: {
      404: '404',
      500: '500',
      pageNotFound: 'Página No Encontrada',
      pageNotFoundMessage: 'La página que buscas no existe o ha sido movida.',
      internalServerError: 'Error Interno del Servidor',
      internalServerErrorMessage: 'Algo salió mal. Por favor, inténtalo de nuevo más tarde.',
      reloadPage: 'Recargar Página'
    },
    common: {
      chooseFile: 'Elige un archivo',
      cancel: 'Cancelar',
      save: 'Guardar',
      add: 'Añadir', // Added
      goHome: 'Ir al inicio',
      goBack: 'Volver',
      profile: 'Perfil',
      logout: 'Cerrar sesión'
    },
    aboutUs: {
      title: 'Acerca de',
      header: 'Acerca de',
      content: 'Ellos nosotros de ellos a lo largo más el más ellos quién tu cómo cuál esto. A lo largo algunos aquellos decir entre otro porque o. El, venir dos decir dar casi gustaría, usar porque, voluntad, pensar arriba, estos ninguno uno tomar decir venir a lo largo no ver. Venir el más tiempo, con, aquí dos su cómo no pero y, algunos él mi hecho hombre porque alrededor, de. Entre sobre también, en tiene a qué, si, debe su el más muy, mi ellos. Y casi ellos ella uno, tenía debajo ellos solo es ambos podría cosa cada ser. El más uno debajo no ver contar bien dos.'
    },
    help: {
      title: 'Ayuda',
      header: 'Ayuda',
      content: 'Ellos nosotros de ellos a lo largo más el más ellos quién tu cómo cuál esto. A lo largo algunos aquellos decir entre otro porque o. El, venir dos decir dar casi gustaría, usar porque, voluntad, pensar arriba, estos ninguno uno tomar decir venir a lo largo no ver. Venir el más tiempo, con, aquí dos su cómo no pero y, algunos él mi hecho hombre porque alrededor, de. Entre sobre también, en tiene a qué, si, debe su el más muy, mi ellos. Y casi ellos ella uno, tenía debajo ellos solo es ambos podría cosa cada ser. El más uno debajo no ver contar bien dos.'
    },
    profile: {
      clickMe: 'Me has pulsado {count} veces'
    },
    login: {
      title: 'Iniciar Sesión',
      subtitle: 'Por favor, inicia sesión para continuar',
      username: 'Usuario',
      password: 'Contraseña',
      login: 'Entrar',
      or: 'O',
      google: 'Iniciar sesión con Google',
      github: 'Iniciar sesión con GitHub',
      noAccount: '¿No tienes una cuenta?',
      createAccount: 'Crea una ahora'
    }
  }
};

export const i18n = createI18n({
  locale: localStorage.getItem('locale') || 'en',
  fallbackLocale: 'en',
  globalInjection: true,
  messages
});
