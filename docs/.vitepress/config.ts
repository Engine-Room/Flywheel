// noinspection JSUnusedGlobalSymbols

import {defineConfig} from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Engine-Room",
  description: "Graphics tech for Minecraft.",

  cleanUrls: true,
  lastUpdated: true,

  head: [["link", { rel: "icon", href: "/engine-room-icon-small.webp" }]],

  transformPageData(pageData, _) {
    const [ base] = pageData.relativePath.split("/")

    if (base == "flywheel") {
      pageData.frontmatter.pageClass = "flywheel"
    } else if (base == "vanillin") {
      pageData.frontmatter.pageClass = "vanillin"
    }
  },

  themeConfig: {
    logo: {
      src: "/engine-room-icon-small.webp",
      width: 24,
      height: 24,
    },

    search: {
      // TODO - Switch to Algolia Search post deployment
      provider: "local",
    },

    // https://vitepress.dev/reference/default-theme-config
    nav: [
      {
        text: "Home",
        link: "/",
      },
      {
        text: 'Flywheel',
        link: '/flywheel/',
        activeMatch: '/flywheel/',
      },
      {
        text: 'Vanillin',
        link: '/vanillin/',
        activeMatch: '/vanillin/',
      },
    ],

    sidebar: {
      "/flywheel/api": [
        {
          text: "Flywheel API",
          link: "/flywheel/api/",
          items: [
            {text: "Concept Reference", link: "/flywheel/api/concepts"},
            { text: "GLSL API", link: "/flywheel/api/glsl-api" },
          ]
        },
      ],
      "/flywheel/tutorial": [
        {
          text: "Flywheel Tutorial",
          items: [
            { text: "Index", link: "/flywheel/tutorial" },
          ]
        },
      ],
      "/vanillin/": [
        {
          text: "Vanillin",
          items: [
            { text: "Settings", link: "/vanillin/settings" },
          ]
        }
      ]
    },

    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/Engine-Room/Flywheel",
      },
      { icon: "discord", link: "https://discord.gg/ambsHEyaAD" },
    ],

    editLink: {
      // Can we get this to automatically pick the main branch?
      pattern: "https://github.com/Engine-Room/Flywheel/edit/1.20.1/dev/docs/:path",
      text: "Edit this page on GitHub",
    },
  },
});
