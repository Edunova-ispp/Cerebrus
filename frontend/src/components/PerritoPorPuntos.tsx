export const getCerberoAsset = (puntos: number = 0): string => {
    if (puntos <= 100) return "/assets/props/perritoCerberito.png";
    if (puntos <= 300) return "/assets/props/perritoCerberito.png";
    if (puntos <= 600) return "/assets/props/perritoCerberito.png";
    if (puntos <= 1000) return "/assets/props/perritoCerberito.png";
    return "/assets/props/perritoCerberito.png";
};