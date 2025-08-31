// noinspection JSUnusedGlobalSymbols

import { defineConfig } from "vitepress";
import tutorial from "./sidebars/tutorial";
import developers from "./sidebars/developers";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Flywheel Docs",
  description: "A modern engine for modded Minecraft.",

  cleanUrls: true,
  lastUpdated: true,

  head: [["link", { rel: "icon", href: "/flywheel-icon-small.webp" }]],

  themeConfig: {
    logo: {
      src: "/flywheel-icon-small.webp",
      width: 24,
      height: 24,
    },

    search: {
      // TODO - Switch to Algolia Search post deployment
      provider: "local",
    },

    // https://vitepress.dev/reference/default-theme-config
    nav: [{ text: "Home", link: "/" }],

    sidebar: {
      ...developers,
      ...tutorial,
    },

    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/Engine-Room/Flywheel",
      },
      { icon: "discord", link: "https://discord.gg/ambsHEyaAD" },
    ],

    editLink: {
      pattern: "https://github.com/Engine-Room/Flywheel/edit/main/docs/:path",
      text: "Edit this page on GitHub",
    },
  },
});
