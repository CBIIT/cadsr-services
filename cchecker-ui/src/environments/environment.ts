// This file can be replaced during build by using the `fileReplacements` array.
// `ng build ---prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  REST_API: 'http://127.0.0.1:8080',
  version: 'VERSIONNUMBER',
  timestamp: '',
  
  cdeBrowserUrl:'https://cdebrowser-dev.nci.nih.gov',
  formBuilderUrl:'https://formbuilder-dev.nci.nih.gov/FormBuilder',
  helpLink:'https://wiki.nci.nih.gov/display/caDSR/caDSR+CDE+Validator'
};

/*
 * In development mode, to ignore zone related error stack frames such as
 * `zone.run`, `zoneDelegate.invokeTask` for easier debugging, you can
 * import the following file, but please comment it out in production mode
 * because it will have performance impact when throw error
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
