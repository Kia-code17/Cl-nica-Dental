-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 22-08-2026 a las 06:48:39
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `clinica_dental`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id`, `nombre`, `descripcion`) VALUES
(1, 'Materiales Dentales', 'Resinas, amalgamas y cementos dentales'),
(2, 'Instrumental', 'Espejos, pinzas, sondas y fórceps'),
(3, 'Higiene y Desinfección', 'Guantes, mascarillas, anestesia y antisépticos'),
(4, 'Ortodoncia', 'Brackets, arcos, ligas y elásticos');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `accesorios`
--

CREATE TABLE `accesorios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `precio_costo` decimal(10,2) NOT NULL DEFAULT 0.00,
  `precio_venta` decimal(10,2) NOT NULL DEFAULT 0.00,
  `stock` int(11) NOT NULL DEFAULT 0,
  `stock_minimo` int(11) NOT NULL DEFAULT 5,
  `categoria_id` int(11) DEFAULT NULL,
  `proveedor_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `accesorios`
--

INSERT INTO `accesorios` (`id`, `nombre`, `descripcion`, `precio_costo`, `precio_venta`, `stock`, `stock_minimo`, `categoria_id`, `proveedor_id`) VALUES
(1, 'Guantes de nitrilo (caja)', 'Caja de 100 unidades', 250.00, 350.00, 40, 10, 3, 1),
(2, 'Anestesia local (caja)', 'Caja de 50 cartuchos', 1800.00, 2400.00, 15, 5, 3, 1),
(3, 'Brackets metálicos (set)', 'Set completo superior/inferior', 1200.00, 2200.00, 20, 5, 4, 2),
(4, 'Resina compuesta', 'Jeringa 4g', 450.00, 700.00, 8, 5, 1, 1),
(5, 'Hilo de sutura', 'Caja de 12 unidades', 300.00, 500.00, 3, 5, 3, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auditoria`
--

CREATE TABLE `auditoria` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `tabla` varchar(80) NOT NULL,
  `accion` varchar(20) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `cambios` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `citas`
--

CREATE TABLE `citas` (
  `id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `estado` enum('Programada','Confirmada','Completada','Cancelada','Inasistencia') DEFAULT 'Programada',
  `notas` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `citas`
--

INSERT INTO `citas` (`id`, `paciente_id`, `doctor_id`, `fecha`, `hora`, `estado`, `notas`) VALUES
(1, 1, 1, '2026-08-21', '09:00:00', 'Confirmada', 'Limpieza dental de rutina'),
(2, 2, 2, '2026-08-21', '10:00:00', 'Programada', 'Revisión de brackets'),
(3, 3, 1, '2026-08-21', '11:00:00', 'Programada', 'Consulta inicial'),
(4, 4, 3, '2026-08-22', '09:30:00', 'Programada', 'Tratamiento de conducto'),
(5, 5, 4, '2026-08-23', '14:00:00', 'Programada', 'Evaluación periodontal');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clinica`
--

CREATE TABLE `clinica` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `rnc` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `clinica`
--

INSERT INTO `clinica` (`id`, `nombre`, `rnc`, `direccion`, `telefono`, `email`) VALUES
(1, 'Clínica Dental UNEV', '101000001', 'Av. Principal #10, Santo Domingo', '809-555-0100', 'contacto@clinicadentalunev.do');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalles_factura`
--

CREATE TABLE `detalles_factura` (
  `id` int(11) NOT NULL,
  `factura_id` int(11) NOT NULL,
  `tratamiento_id` int(11) DEFAULT NULL,
  `accesorio_id` int(11) DEFAULT NULL,
  `descripcion` varchar(255) NOT NULL DEFAULT '',
  `cantidad` int(11) NOT NULL DEFAULT 1,
  `precio` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `detalles_factura`
--

INSERT INTO `detalles_factura` (`id`, `factura_id`, `tratamiento_id`, `accesorio_id`, `descripcion`, `cantidad`, `precio`) VALUES
(1, 1, 1, NULL, 'Limpieza dental (profilaxis)', 1, 1500.00),
(2, 2, 2, NULL, 'Ajuste de ortodoncia', 1, 2000.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `doctores`
--

CREATE TABLE `doctores` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `cedula` varchar(20) NOT NULL,
  `especialidad` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `doctores`
--

INSERT INTO `doctores` (`id`, `nombre`, `cedula`, `especialidad`, `telefono`, `email`, `usuario_id`, `activo`) VALUES
(1, 'Dr. Ramón Peña', '001-1111111-1', 'Odontología General', '809-555-0111', 'r.pena@clinicadentalunev.do', 4, 1),
(2, 'Dra. Lucía Fernández', '001-2222222-2', 'Ortodoncia', '809-555-0112', 'l.fernandez@clinicadentalunev.do', 5, 1),
(3, 'Dr. Marcos Villanueva', '001-3333333-3', 'Endodoncia', '809-555-0113', 'm.villanueva@clinicadentalunev.do', NULL, 1),
(4, 'Dra. Paola Reyes', '001-4444444-4', 'Periodoncia', '809-555-0114', 'p.reyes@clinicadentalunev.do', NULL, 1),
(5, 'Dr. Iván Castro', '001-5555555-5', 'Cirugía Maxilofacial', '809-555-0115', 'i.castro@clinicadentalunev.do', NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `facturas`
--

CREATE TABLE `facturas` (
  `id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `cita_id` int(11) DEFAULT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00,
  `descuento_porcentaje` decimal(5,2) NOT NULL DEFAULT 0.00,
  `descuento_monto` decimal(10,2) NOT NULL DEFAULT 0.00,
  `impuesto_porcentaje` decimal(5,2) NOT NULL DEFAULT 0.00,
  `impuesto_monto` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado_pago` enum('Pendiente','Pagada','Parcial','Anulada') DEFAULT 'Pendiente',
  `metodo_pago_preferido` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `facturas`
--

INSERT INTO `facturas` (`id`, `paciente_id`, `cita_id`, `fecha`, `subtotal`, `descuento_porcentaje`, `descuento_monto`, `impuesto_porcentaje`, `impuesto_monto`, `total`, `estado_pago`, `metodo_pago_preferido`) VALUES
(1, 1, 1, '2026-08-21 18:51:23', 1500.00, 0.00, 0.00, 0.00, 0.00, 1500.00, 'Pagada', 'Tarjeta'),
(2, 2, 2, '2026-08-21 18:51:23', 2000.00, 0.00, 0.00, 0.00, 0.00, 2000.00, 'Pendiente', 'Efectivo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historiales_medicos`
--

CREATE TABLE `historiales_medicos` (
  `id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `notas` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarios_doctores`
--

CREATE TABLE `horarios_doctores` (
  `id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `dia` enum('Lunes','Martes','Miercoles','Jueves','Viernes','Sabado','Domingo') NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `horarios_doctores`
--

INSERT INTO `horarios_doctores` (`id`, `doctor_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES
(1, 1, 'Lunes', '08:00:00', '16:00:00'),
(2, 1, 'Miercoles', '08:00:00', '16:00:00'),
(3, 2, 'Martes', '09:00:00', '17:00:00'),
(4, 2, 'Jueves', '09:00:00', '17:00:00'),
(5, 3, 'Lunes', '13:00:00', '19:00:00'),
(6, 4, 'Viernes', '08:00:00', '14:00:00'),
(7, 5, 'Sabado', '08:00:00', '12:00:00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_inventario`
--

CREATE TABLE `movimientos_inventario` (
  `id` int(11) NOT NULL,
  `accesorio_id` int(11) NOT NULL,
  `tipo` enum('Entrada','Salida','Ajuste','Venta') NOT NULL,
  `cantidad` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `nota` varchar(255) DEFAULT NULL,
  `factura_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `movimientos_inventario`
--

INSERT INTO `movimientos_inventario` (`id`, `accesorio_id`, `tipo`, `cantidad`, `fecha`, `nota`, `factura_id`) VALUES
(1, 1, 'Entrada', 50, '2026-08-21 18:51:23', 'Compra inicial de stock', NULL),
(2, 5, 'Salida', 9, '2026-08-21 18:51:23', 'Consumo en procedimientos quirúrgicos', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `odontogramas`
--

CREATE TABLE `odontogramas` (
  `id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `diente_id` tinyint(4) NOT NULL,
  `estado` enum('Sano','Cariado','Obturado','Extraido','Corona','Endodoncia','Implante') NOT NULL,
  `notas` text DEFAULT NULL,
  `foto_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pacientes`
--

CREATE TABLE `pacientes` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `cedula` varchar(20) NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `alergias` text DEFAULT NULL,
  `foto_url` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1,
  `fecha_registro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pacientes`
--

INSERT INTO `pacientes` (`id`, `nombre`, `cedula`, `fecha_nacimiento`, `telefono`, `email`, `direccion`, `alergias`, `foto_url`, `activo`, `fecha_registro`) VALUES
(1, 'María Gómez', '001-1000001-1', '1990-02-15', '809-555-1001', 'maria.gomez@mail.com', 'Calle 1 #10', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(2, 'Carlos Sánchez', '001-1000002-2', '1985-07-22', '809-555-1002', 'carlos.sanchez@mail.com', 'Calle 2 #20', 'Penicilina', NULL, 1, '2026-08-21 18:51:23'),
(3, 'Ana Martínez', '001-1000003-3', '2000-11-05', '809-555-1003', 'ana.martinez@mail.com', 'Calle 3 #30', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(4, 'Pedro Ramírez', '001-1000004-4', '1978-03-30', '809-555-1004', 'pedro.ramirez@mail.com', 'Calle 4 #40', 'Látex', NULL, 1, '2026-08-21 18:51:23'),
(5, 'Luisa Fernández', '001-1000005-5', '1995-09-18', '809-555-1005', 'luisa.fernandez@mail.com', 'Calle 5 #50', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(6, 'Jorge Díaz', '001-1000006-6', '1982-12-01', '809-555-1006', 'jorge.diaz@mail.com', 'Calle 6 #60', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(7, 'Rosa Herrera', '001-1000007-7', '1999-05-27', '809-555-1007', 'rosa.herrera@mail.com', 'Calle 7 #70', 'Ibuprofeno', NULL, 1, '2026-08-21 18:51:23'),
(8, 'Miguel Torres', '001-1000008-8', '1970-01-10', '809-555-1008', 'miguel.torres@mail.com', 'Calle 8 #80', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(9, 'Elena Vargas', '001-1000009-9', '1988-08-08', '809-555-1009', 'elena.vargas@mail.com', 'Calle 9 #90', 'Ninguna', NULL, 1, '2026-08-21 18:51:23'),
(10, 'Antonio Cruz', '001-1000010-0', '1993-04-14', '809-555-1010', 'antonio.cruz@mail.com', 'Calle 10 #100', 'Ninguna', NULL, 1, '2026-08-21 18:51:23');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos`
--

CREATE TABLE `pagos` (
  `id` int(11) NOT NULL,
  `factura_id` int(11) NOT NULL,
  `fecha` datetime DEFAULT current_timestamp(),
  `monto` decimal(10,2) NOT NULL,
  `metodo` enum('Efectivo','Tarjeta','Transferencia','Cheque') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pagos`
--

INSERT INTO `pagos` (`id`, `factura_id`, `fecha`, `monto`, `metodo`) VALUES
(1, 1, '2026-08-21 18:51:23', 1500.00, 'Tarjeta');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permisos`
--

CREATE TABLE `permisos` (
  `id` int(11) NOT NULL,
  `rol_id` int(11) NOT NULL,
  `recurso` varchar(80) NOT NULL,
  `accion` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `proveedores`
--

CREATE TABLE `proveedores` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `contacto` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `proveedores`
--

INSERT INTO `proveedores` (`id`, `nombre`, `contacto`, `telefono`, `email`) VALUES
(1, 'Dental Supply RD', 'Wanda Lora', '809-555-2001', 'ventas@dentalsupplyrd.com'),
(2, 'OrtoMateriales SRL', 'Hector Pujols', '809-555-2002', 'contacto@ortomateriales.com');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id`, `nombre`, `descripcion`) VALUES
(1, 'Admin', 'Acceso total al sistema'),
(2, 'Doctor', 'Acceso a pacientes, citas y odontograma'),
(3, 'Recepcionista', 'Acceso a agenda, pacientes y facturación'),
(4, 'Asistente', 'Acceso limitado de apoyo clínico');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tratamientos`
--

CREATE TABLE `tratamientos` (
  `id` int(11) NOT NULL,
  `cita_id` int(11) NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `costo` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tratamientos`
--

INSERT INTO `tratamientos` (`id`, `cita_id`, `descripcion`, `costo`) VALUES
(1, 1, 'Limpieza dental (profilaxis)', 1500.00),
(2, 2, 'Ajuste de ortodoncia', 2000.00),
(3, 3, 'Consulta y diagnóstico', 800.00),
(4, 4, 'Tratamiento de conducto', 8500.00),
(5, 5, 'Evaluación periodontal', 1200.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nombre_completo` varchar(150) NOT NULL,
  `rol_id` int(11) NOT NULL,
  `activo` tinyint(1) DEFAULT 1,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `ultimo_login` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `usuario`, `password_hash`, `nombre_completo`, `rol_id`, `activo`, `fecha_creacion`, `ultimo_login`) VALUES
(1, 'admin', 'admin123', 'Administrador General', 1, 1, '2026-08-21 18:51:23', NULL),
(2, 't.araujo', 'araujo123', 'Tashia Margarita Araujo', 1, 1, '2026-08-21 18:51:23', NULL),
(3, 'k.castillo', 'castillo123', 'Kiany Castillo Gomez', 3, 1, '2026-08-21 18:51:23', '2026-08-21 23:00:06'),
(4, 'a.guzman', 'guzman123', 'Alam Ivan Guzman Beato', 2, 1, '2026-08-21 18:51:23', NULL),
(5, 's.mercedes', 'mercedes123', 'Stuard Altemar Mercedes Mena', 2, 1, '2026-08-21 18:51:23', NULL),
(6, 'j.padilla', 'padilla123', 'John Manuel Padilla Caba', 3, 1, '2026-08-21 18:51:23', NULL),
(7, 'e.piefil', 'pietri123', 'Emilka Piefil', 1, 1, '2026-08-21 18:51:23', NULL);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_citas_del_dia`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_citas_del_dia` (
`id` int(11)
,`paciente` varchar(150)
,`doctor` varchar(150)
,`fecha` date
,`hora` time
,`estado` enum('Programada','Confirmada','Completada','Cancelada','Inasistencia')
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_estado_cuenta_paciente`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_estado_cuenta_paciente` (
`paciente_id` int(11)
,`paciente` varchar(150)
,`total_facturado` decimal(32,2)
,`total_pagado` decimal(32,2)
,`saldo_pendiente` decimal(33,2)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_inventario_bajo_stock`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_inventario_bajo_stock` (
`id` int(11)
,`nombre` varchar(150)
,`stock` int(11)
,`stock_minimo` int(11)
);

-- --------------------------------------------------------

--
-- Estructura para la vista `v_citas_del_dia`
--
DROP TABLE IF EXISTS `v_citas_del_dia`;

CREATE VIEW `v_citas_del_dia` AS SELECT `c`.`id` AS `id`, `p`.`nombre` AS `paciente`, `d`.`nombre` AS `doctor`, `c`.`fecha` AS `fecha`, `c`.`hora` AS `hora`, `c`.`estado` AS `estado` FROM ((`citas` `c` join `pacientes` `p` on(`p`.`id` = `c`.`paciente_id`)) join `doctores` `d` on(`d`.`id` = `c`.`doctor_id`)) WHERE `c`.`fecha` = curdate() ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_estado_cuenta_paciente`
--
DROP TABLE IF EXISTS `v_estado_cuenta_paciente`;

CREATE VIEW `v_estado_cuenta_paciente` AS SELECT `f`.`paciente_id` AS `paciente_id`, `p`.`nombre` AS `paciente`, sum(`f`.`total`) AS `total_facturado`, coalesce(sum(`pg`.`monto`),0) AS `total_pagado`, sum(`f`.`total`) - coalesce(sum(`pg`.`monto`),0) AS `saldo_pendiente` FROM ((`facturas` `f` join `pacientes` `p` on(`p`.`id` = `f`.`paciente_id`)) left join `pagos` `pg` on(`pg`.`factura_id` = `f`.`id`)) GROUP BY `f`.`paciente_id`, `p`.`nombre` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_inventario_bajo_stock`
--
DROP TABLE IF EXISTS `v_inventario_bajo_stock`;

CREATE VIEW `v_inventario_bajo_stock` AS SELECT `accesorios`.`id` AS `id`, `accesorios`.`nombre` AS `nombre`, `accesorios`.`stock` AS `stock`, `accesorios`.`stock_minimo` AS `stock_minimo` FROM `accesorios` WHERE `accesorios`.`stock` <= `accesorios`.`stock_minimo` ;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `accesorios`
--
ALTER TABLE `accesorios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categoria_id` (`categoria_id`),
  ADD KEY `proveedor_id` (`proveedor_id`);

--
-- Indices de la tabla `auditoria`
--
ALTER TABLE `auditoria`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `citas`
--
ALTER TABLE `citas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `paciente_id` (`paciente_id`),
  ADD KEY `idx_cita_fecha` (`fecha`),
  ADD KEY `idx_cita_doctor_fecha` (`doctor_id`,`fecha`);

--
-- Indices de la tabla `clinica`
--
ALTER TABLE `clinica`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `detalles_factura`
--
ALTER TABLE `detalles_factura`
  ADD PRIMARY KEY (`id`),
  ADD KEY `factura_id` (`factura_id`),
  ADD KEY `tratamiento_id` (`tratamiento_id`),
  ADD KEY `accesorio_id` (`accesorio_id`);

--
-- Indices de la tabla `doctores`
--
ALTER TABLE `doctores`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cedula` (`cedula`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `facturas`
--
ALTER TABLE `facturas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cita_id` (`cita_id`),
  ADD KEY `idx_factura_paciente` (`paciente_id`);

--
-- Indices de la tabla `historiales_medicos`
--
ALTER TABLE `historiales_medicos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_id` (`doctor_id`),
  ADD KEY `idx_historial_paciente` (`paciente_id`);

--
-- Indices de la tabla `horarios_doctores`
--
ALTER TABLE `horarios_doctores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Indices de la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD PRIMARY KEY (`id`),
  ADD KEY `accesorio_id` (`accesorio_id`);

--
-- Indices de la tabla `odontogramas`
--
ALTER TABLE `odontogramas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_id` (`doctor_id`),
  ADD KEY `idx_odontograma_paciente` (`paciente_id`);

--
-- Indices de la tabla `pacientes`
--
ALTER TABLE `pacientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cedula` (`cedula`),
  ADD KEY `idx_paciente_nombre` (`nombre`),
  ADD KEY `idx_paciente_cedula` (`cedula`);

--
-- Indices de la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `factura_id` (`factura_id`);

--
-- Indices de la tabla `permisos`
--
ALTER TABLE `permisos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_rol_recurso_accion` (`rol_id`,`recurso`,`accion`);

--
-- Indices de la tabla `proveedores`
--
ALTER TABLE `proveedores`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `tratamientos`
--
ALTER TABLE `tratamientos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cita_id` (`cita_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario` (`usuario`),
  ADD KEY `rol_id` (`rol_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `accesorios`
--
ALTER TABLE `accesorios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `auditoria`
--
ALTER TABLE `auditoria`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `citas`
--
ALTER TABLE `citas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `clinica`
--
ALTER TABLE `clinica`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `detalles_factura`
--
ALTER TABLE `detalles_factura`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `doctores`
--
ALTER TABLE `doctores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `facturas`
--
ALTER TABLE `facturas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `historiales_medicos`
--
ALTER TABLE `historiales_medicos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `horarios_doctores`
--
ALTER TABLE `horarios_doctores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `odontogramas`
--
ALTER TABLE `odontogramas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `pacientes`
--
ALTER TABLE `pacientes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `pagos`
--
ALTER TABLE `pagos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `permisos`
--
ALTER TABLE `permisos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `proveedores`
--
ALTER TABLE `proveedores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `tratamientos`
--
ALTER TABLE `tratamientos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `accesorios`
--
ALTER TABLE `accesorios`
  ADD CONSTRAINT `accesorios_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `accesorios_ibfk_2` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `auditoria`
--
ALTER TABLE `auditoria`
  ADD CONSTRAINT `auditoria_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `citas`
--
ALTER TABLE `citas`
  ADD CONSTRAINT `citas_ibfk_1` FOREIGN KEY (`paciente_id`) REFERENCES `pacientes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `citas_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctores` (`id`);

--
-- Filtros para la tabla `detalles_factura`
--
ALTER TABLE `detalles_factura`
  ADD CONSTRAINT `detalles_factura_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `detalles_factura_ibfk_2` FOREIGN KEY (`tratamiento_id`) REFERENCES `tratamientos` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `detalles_factura_ibfk_3` FOREIGN KEY (`accesorio_id`) REFERENCES `accesorios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `doctores`
--
ALTER TABLE `doctores`
  ADD CONSTRAINT `doctores_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `facturas`
--
ALTER TABLE `facturas`
  ADD CONSTRAINT `facturas_ibfk_1` FOREIGN KEY (`paciente_id`) REFERENCES `pacientes` (`id`),
  ADD CONSTRAINT `facturas_ibfk_2` FOREIGN KEY (`cita_id`) REFERENCES `citas` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `historiales_medicos`
--
ALTER TABLE `historiales_medicos`
  ADD CONSTRAINT `historiales_medicos_ibfk_1` FOREIGN KEY (`paciente_id`) REFERENCES `pacientes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `historiales_medicos_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctores` (`id`);

--
-- Filtros para la tabla `horarios_doctores`
--
ALTER TABLE `horarios_doctores`
  ADD CONSTRAINT `horarios_doctores_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctores` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD CONSTRAINT `movimientos_inventario_ibfk_1` FOREIGN KEY (`accesorio_id`) REFERENCES `accesorios` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `movimientos_inventario_ibfk_2` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `odontogramas`
--
ALTER TABLE `odontogramas`
  ADD CONSTRAINT `odontogramas_ibfk_1` FOREIGN KEY (`paciente_id`) REFERENCES `pacientes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `odontogramas_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctores` (`id`);

--
-- Filtros para la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD CONSTRAINT `pagos_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `permisos`
--
ALTER TABLE `permisos`
  ADD CONSTRAINT `permisos_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `tratamientos`
--
ALTER TABLE `tratamientos`
  ADD CONSTRAINT `tratamientos_ibfk_1` FOREIGN KEY (`cita_id`) REFERENCES `citas` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
