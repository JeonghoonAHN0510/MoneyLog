"use client";

import * as React from "react";
import * as TabsPrimitive from "@radix-ui/react-tabs";

import { cn } from "./utils";

function Tabs({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Root>) {
  return (
    <TabsPrimitive.Root
      data-slot="tabs"
      className={cn("flex flex-col gap-2", className)}
      {...props}
    />
  );
}

function TabsList({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.List>) {
  return (
    <TabsPrimitive.List
      data-slot="tabs-list"
      className={cn(
        "text-muted-foreground inline-flex min-h-11 w-fit items-center justify-center rounded-[1.25rem] border border-white/55 bg-white/68 p-1 shadow-[var(--shadow-soft)] backdrop-blur-xl flex",
        className,
      )}
      {...props}
    />
  );
}

function TabsTrigger({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Trigger>) {
  return (
    <TabsPrimitive.Trigger
      data-slot="tabs-trigger"
      className={cn(
        "data-[state=active]:border-white/70 data-[state=active]:bg-white data-[state=active]:text-foreground data-[state=active]:shadow-[0_10px_24px_rgba(15,42,88,0.12)] focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:outline-ring text-foreground inline-flex h-[calc(100%-2px)] flex-1 items-center justify-center gap-1.5 rounded-[1rem] border border-transparent px-3 py-2 text-sm font-medium whitespace-nowrap transition-[color,box-shadow,transform] focus-visible:ring-[3px] focus-visible:outline-1 disabled:pointer-events-none disabled:opacity-50 hover:translate-y-[-1px] [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className,
      )}
      {...props}
    />
  );
}

function TabsContent({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Content>) {
  return (
    <TabsPrimitive.Content
      data-slot="tabs-content"
      className={cn("flex-1 outline-none", className)}
      {...props}
    />
  );
}

export { Tabs, TabsList, TabsTrigger, TabsContent };
