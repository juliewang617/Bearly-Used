// this is needed to be able to import the logo 
declare module '*.png' {
    const src: string;
    export default src;
  }